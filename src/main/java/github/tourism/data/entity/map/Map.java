package github.tourism.data.entity.map;


import github.tourism.data.entity.favPlace.FavPlace;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "Map")
public class Map {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_id")
    private Integer mapId;

    @OneToMany(mappedBy = "map")
    private List<FavPlace> favPlaces = new ArrayList<>();

    private String place_name;
    private String place_image;
    private String place_info;
    private String operation_time;
    private String close_date;
    private String place_location;
    private String place_tel;
    private String category;
    private BigDecimal lat;
    private BigDecimal lng;

    //  찜 카운트 필드
    @Column(name = "likemark_count", nullable = false)
    private Integer likemarkCount = 0;

    // 찜 카운트 증가 메서드
    public void incrementLikemarkCount() {
        this.likemarkCount++;
    }

    // 찜 카운트 감소 메서드
    public void decrementLikemarkCount() {
        if (this.likemarkCount > 0) {
            this.likemarkCount--;
        }
    }

    // 정적 팩토리 메서드 추가 (테스트용)
    public static Map createTestMap(String category) {
        Map map = new Map();
        map.category = category;
        return map;
    }


}