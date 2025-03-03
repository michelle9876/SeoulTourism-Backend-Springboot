package github.tourism.data.repository.map;

import github.tourism.data.entity.map.Map;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MapRepository extends JpaRepository<Map, Integer> {

    //카테고리별로 조회
    Page<Map> findByCategory(Pageable pageable, String category);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Map m WHERE m.mapId = :mapId")
    Optional<Map> findWithLockById(@Param("mapId") Integer mapId);

    @Modifying
    @Query("UPDATE Map m SET m.likemarkCount = m.likemarkCount + 1 WHERE m.mapId = :mapId")
    void incrementLikemarkCount(@Param("mapId") Integer mapId);

    @Modifying
    @Query("UPDATE Map m SET m.likemarkCount = m.likemarkCount - 1 WHERE m.mapId = :mapId AND m.likemarkCount > 0")
    void decrementLikemarkCount(@Param("mapId") Integer mapId);

    List<Map> findByCategory(String category);

    // placeName으로 likeMarkCount 조회
    @Query("SELECT m FROM Map m WHERE m.place_name = :placeName")
    Optional <Map> findMapByPlaceName(@Param("placeName") String placeName);

    @Query("SELECT m.likemarkCount FROM Map m WHERE m.place_name = :placeName")
    Optional<Integer> findLikesByPlaceName(@Param("placeName") String placeName);
}
