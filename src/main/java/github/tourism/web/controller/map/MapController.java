package github.tourism.web.controller.map;

import github.tourism.service.map.MapService;
import github.tourism.service.user.security.CustomUserDetails;
import github.tourism.web.dto.map.MapDetailsDTO;
import github.tourism.web.dto.map.MapsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/maps")
public class MapController {

    private final MapService mapService;


    //전체 관광지 조회
    @GetMapping
    public ResponseEntity<Map<String,Object>> getAllMaps(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size)
    {
        Page<MapsDTO> allMaps = mapService.getAllMaps(page, size);

        //프론트측에서 요청한 양식대로 변경
        Map<String, Object> response = new HashMap<>();
        response.put("totalPages", allMaps.getTotalPages());
        response.put("currentPage", allMaps.getNumber());
        response.put("maps", allMaps.getContent());

        return ResponseEntity.ok(response);
    }

    //카테고리별로 조회
    @GetMapping("/category/{category}")
    public ResponseEntity<PagedModel<MapsDTO>> getMapsByCategory(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size,
            @PathVariable String category) {
        Page<MapsDTO> mapsByCategory = mapService.getMapsByCategory(page, size, category);
        return ResponseEntity.ok(new PagedModel<>(mapsByCategory));
    }


    //맵 상세조회
    @GetMapping("/{mapId}")
    public ResponseEntity<MapDetailsDTO> getMapDetail(@PathVariable Integer mapId,
                                                      @AuthenticationPrincipal CustomUserDetails user) {
        Integer userId = null;
        if(user != null){
             userId = Integer.valueOf(user.getUserId());
        }
        MapDetailsDTO mapDetails = mapService.getMapDetail(mapId,userId);
        return ResponseEntity.ok(mapDetails);
    }


    //찜 토글하기
    @PreAuthorize("isAuthenticated()") //로그인 체크
    @PostMapping("/{mapId}/new")
    public ResponseEntity<Boolean> toggleFavPlace(@AuthenticationPrincipal CustomUserDetails user,
                                                    @PathVariable Integer mapId) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Integer userId = user.getUserId();
        boolean isFavorite = mapService.toggleFavoritePlace(userId, mapId);

        return ResponseEntity.ok(isFavorite);
    }

    // placeName으로 likeMarkCount 조회
    @GetMapping("/{placeName}/likes")
    public ResponseEntity<Integer> getPlaceLikes(@PathVariable String placeName) {
        Integer likeMarkCount = mapService.getLikesByPlaceName(placeName);
        return ResponseEntity.ok(likeMarkCount);
    }

    // 찜 카운트 증가 API
    @PostMapping("/{placeName}/likes/increment")
    public ResponseEntity<Integer> incrementLikes(@PathVariable String placeName) {
        Integer likeMarkCount = mapService.incrementLikes(placeName);
        return ResponseEntity.ok(likeMarkCount);
    }

    //  찜 카운트 감소 API
    @PostMapping("/{placeName}/likes/decrement")
    public ResponseEntity<Integer> decrementLikes(@PathVariable String placeName) {
        Integer likeMarkCount = mapService.decrementLikes(placeName);
        return ResponseEntity.ok(likeMarkCount);
    }

}
