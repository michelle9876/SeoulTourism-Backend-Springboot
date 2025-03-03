package github.tourism.service.favPlace;

import github.tourism.data.entity.favPlace.FavPlace;
import github.tourism.data.entity.map.Map;
import github.tourism.data.entity.user.User;
import github.tourism.data.repository.favPlace.FavPlaceRepository;
import github.tourism.data.repository.map.MapRepository;
import github.tourism.data.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavPlaceService {
    private final FavPlaceRepository favPlaceRepository;
    private final MapRepository mapRepository;
    private final UserRepository userRepository;
    // redis
    private final RedisTemplate<String, String> redisTemplate;

    // redis
    private String getRedisKey(Integer mapId) {
        return "place:" + mapId;
    }



//    1.	찜한 장소 저장: 사용자가 특정 장소를 찜하면 FavPlace 테이블에 저장.
//	  2.	찜 카운트 증가: 찜할 때마다 Map 테이블에서 해당 장소의 찜 카운트를 증가.
//	  3.	중복 찜 방지: 사용자가 이미 찜한 경우 중복으로 추가되지 않도록 처리.
//    4.	찜 해제 기능: 사용자가 찜을 해제하면 카운트를 감소시키고, FavPlace 테이블에서 삭제.



    // 유저의 찜한 장소 가져오기
    @Transactional(readOnly = true)
    public Page<FavPlace> getUserFavPlaces(Integer userId, Pageable pageable) {
        return favPlaceRepository.findAllByUserUserId(userId, pageable);
    }

    // 찜한 장소 저장
    @Transactional
    public void addFavPlace(Integer userId, Integer mapId){

        // User를 데이터베이스에서 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + userId));

        // Map 데이터베이스에서 조회 with Pessimistic Lock
        Map map = mapRepository.findWithLockById(mapId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다. ID: " + mapId));

        // 중복 찜 방지
        if(favPlaceRepository.existsByUserUserIdAndMapMapId(userId, mapId)){
            throw new IllegalArgumentException("이미 찜한 장소입니다.");
        }

        // FavPlace 생성 및 저장
        FavPlace favPlace = new FavPlace();
        favPlace.setMap(map);
        favPlace.setUser(user);
        favPlace.setPlaceName(map.getPlace_name());
        favPlace.setPlaceLocation(map.getPlace_location());
        favPlace.setPlaceImage(map.getPlace_image());
//        favPlace.setPlaceDetailsInfo(map.getPlace_info());
        favPlace.setLikeStatus(true);
//        favPlace.setLikemarkCount(0); // 초기값 설정

        favPlaceRepository.save(favPlace);

        // Redis에서 찜 개수 증가
        String key = getRedisKey(mapId);
        redisTemplate.opsForValue().increment(key);


        // 장소를 다른 사용자가 찜했으므로 likemarkCount 증가
        mapRepository.incrementLikemarkCount(mapId);
    }

    // 찜한 장소 삭제
    @Transactional
    public void deleteFavPlace(Integer favPlaceId) {
        // FavPlace존재 확인
        FavPlace favPlace = favPlaceRepository.findById(favPlaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 찜한 장소가 존재하지 않습니다. ID: " + favPlaceId));

        // Map ID 가져오기
        Integer mapId = favPlace.getMap().getMapId();

        // FavPlace 삭제
        favPlaceRepository.deleteById(favPlaceId);

        // Redis에서 찜 개수 감소
        String key = getRedisKey(mapId);
        redisTemplate.opsForValue().decrement(key);

        // Map의 likemarkCount 감소
        mapRepository.decrementLikemarkCount(mapId);

    }

    // 사용자 ID와 Map ID를 이용한 찜 해제
    @Transactional
    public void removeFavPlaceByUserAndMap(Integer userId, Integer mapId) {
        FavPlace favPlace = favPlaceRepository.findByUserUserIdAndMapMapId(userId, mapId)
                .orElseThrow(() -> new IllegalArgumentException("찜하지 않은 장소입니다."));

        // FavPlace 삭제
        favPlaceRepository.delete(favPlace);

        // Redis에서 찜 개수 감소
        String key = getRedisKey(mapId);
        redisTemplate.opsForValue().decrement(key);


        // Map의 likemarkCount 감소
        mapRepository.decrementLikemarkCount(mapId);
    }

    // Redis에서 찜 개수 조회
    public Integer getLikes(Integer mapId) {
        String key = getRedisKey(mapId);
        String likes = redisTemplate.opsForValue().get(key);
        return likes != null ? Integer.parseInt(likes) : 0;
    }


}
