package github.tourism.service.map;

import github.tourism.data.entity.favPlace.FavPlace;
import github.tourism.data.entity.map.Map;
import github.tourism.data.entity.user.User;
import github.tourism.data.repository.favPlace.FavPlaceRepository;
import github.tourism.data.repository.map.MapRepository;
import github.tourism.data.repository.user.UserRepository;
import github.tourism.service.redis.RedisLikeService;
import github.tourism.web.advice.ErrorCode;
import github.tourism.web.dto.map.MapDetailsDTO;
import github.tourism.web.dto.map.MapsDTO;
import github.tourism.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
//@RequiredArgsConstructor
public class MapService {

    private final MapRepository mapRepository;
    private final FavPlaceRepository favPlaceRepository;
    private final UserRepository userRepository;
//    private final RedisTemplate<String, String> redisTemplate;
    private final RedisLikeService redisLikeService;

    public MapService(MapRepository mapRepository, FavPlaceRepository favPlaceRepository, UserRepository userRepository, RedisLikeService redisLikeService) {
        this.mapRepository = mapRepository;
        this.favPlaceRepository = favPlaceRepository;
        this.userRepository = userRepository;
        this.redisLikeService = redisLikeService;
    }


//    private String getRedisKey(String placeName) {
//        return "place:likes:" + placeName;
//    }

    //전체 조회
    public Page<MapsDTO> getAllMaps(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Map> maps = mapRepository.findAll(pageable);
        Page<MapsDTO> mapPage = maps.map(MapsDTO::new);

        return mapPage;
    }

    //카테고리별로 맵 조회
    public Page<MapsDTO> getMapsByCategory(int page, int size, String category) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Map> mapsBycategory = mapRepository.findByCategory(pageable, category);
        Page<MapsDTO> mapsDTOPage = mapsBycategory.map(MapsDTO::new);
        return mapsDTOPage;
    }

    //상세 페이지 조회
    public MapDetailsDTO getMapDetail(Integer mapId, Integer userId) {
        Map map = mapRepository.findById(mapId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MAPS_NOT_FOUNDED));

        MapDetailsDTO mapDetailsDTO = new MapDetailsDTO(map);

        //사용자가 로그인 했는지 체크여부에 따라 찜 여부 확인
        if (userId != null) {
            boolean isFavorite = favPlaceRepository.existsByUserUserIdAndMapMapId(userId, mapId);
            mapDetailsDTO.setFavorite(isFavorite);
        } else {
            mapDetailsDTO.setFavorite(false); // 로그인되지 않은 경우
        }

        return mapDetailsDTO;
    }
    
    //찜 Toggle
    @Transactional
    public boolean toggleFavoritePlace(Integer userId, Integer mapId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUNDED));

        Map map = mapRepository.findById(mapId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MAPS_NOT_FOUNDED));

        Optional<FavPlace> findFavPlace = favPlaceRepository.findByUserAndMap(user, map);


        if (findFavPlace.isPresent()) {
            //찜 제거
            favPlaceRepository.delete(findFavPlace.get());
            return false;
        } else {
            // 찜 등록
            FavPlace favPlace = new FavPlace(map,user);
            favPlaceRepository.save(favPlace);
            return true;
        }
    }

//    // placeName으로 likeMarkCount 조회
//    public Integer getLikesByPlaceName(String placeName) {
//        Map map = mapRepository.findMapByPlaceName(placeName)
//                .orElseThrow(() -> new IllegalArgumentException("찜한 장소가 없습니다."));
//
//        return map.getLikemarkCount();
//    }

    // 찜 카운트 조회 (Redis → DB 순서로 조회)
    @Transactional(readOnly = true)
    public Integer getLikesByPlaceName(String placeName) {
        // 1. Redis에서 조회
        Integer cachedLikeCount = redisLikeService.getLikeCount(placeName);
        if (cachedLikeCount != null) {
            return cachedLikeCount;
        }

        // 2. Redis에 없으면 DB에서 조회 후 캐싱
        Map map = mapRepository.findMapByPlaceName(placeName)
                .orElseThrow(() -> new IllegalArgumentException("찜한 장소가 없습니다."));

        Integer likeCount = map.getLikemarkCount();
        redisLikeService.saveLikeCount(placeName, likeCount); // Redis에 캐싱
        return likeCount;
    }

    // 찜 카운트 증가
    @Transactional
    public Integer incrementLikes(String placeName) {
        Integer updatedLikeCount = redisLikeService.incrementLikeCount(placeName);

        // Redis에 값이 없을 수도 있으므로, MySQL에서 최신 값을 가져와서 업데이트
        if (updatedLikeCount == 1) {
            Map map = mapRepository.findMapByPlaceName(placeName)
                    .orElseThrow(() -> new IllegalArgumentException("찜한 장소가 없습니다."));
            updatedLikeCount = map.getLikemarkCount() + 1;
            redisLikeService.saveLikeCount(placeName, updatedLikeCount);
        }

        return updatedLikeCount;
    }

    //  찜 카운트 감소
    @Transactional
    public Integer decrementLikes(String placeName) {
        Integer updatedLikeCount = redisLikeService.decrementLikeCount(placeName);

        if (updatedLikeCount == -1) { // Redis에 데이터가 없던 경우
            Map map = mapRepository.findMapByPlaceName(placeName)
                    .orElseThrow(() -> new IllegalArgumentException("찜한 장소가 없습니다."));
            updatedLikeCount = Math.max(map.getLikemarkCount() - 1, 0);
            redisLikeService.saveLikeCount(placeName, updatedLikeCount);
        }

        return updatedLikeCount;
    }


//    // 특정 장소의 찜 개수 조회 (Redis 적용)
//    @Transactional(readOnly = true)
//    public int getLikesByPlaceName(String placeName) {
//        String key = getRedisKey(placeName);
//
//        // 1 Redis에서 조회
//        String cachedLikes = redisTemplate.opsForValue().get(key);
//        if (cachedLikes != null) {
//            return Integer.parseInt(cachedLikes);
//        }
//
//        // 2 Redis에 없으면 DB 조회
//        int likes = mapRepository.findLikesByPlaceName(placeName)
//                .orElse(0); // 데이터가 없으면 기본값 0
//
//        //  3 Redis에 저장 (TTL 30초 설정 가능)
//        redisTemplate.opsForValue().set(key, String.valueOf(likes), Duration.ofSeconds(30));
//
//        return likes;
//    }


}
