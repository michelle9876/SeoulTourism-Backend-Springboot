package github.tourism.data.entity.calendar;

import github.tourism.data.entity.favPlace.FavPlace;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Calendar")
public class Calendar {

    @Id
    @Column(name = "calendar_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer calendarId ;

    @Column(name = "user_id")
    private Integer userId ;

    @Column(name = "map_id")
    private Integer mapId ;

    @Column(name = "fav_place_id") // 외래 키를 직접 다룰 수 있도록 추가
    private Integer favPlaceId;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "fav_place_id", referencedColumnName = "fav_place_id", insertable = false, updatable = false)
    private FavPlace favPlace ;

    @Column(name = "tour_start_date")
    private LocalDate tourStartDate ;

//    @Column(name = "place_name")
//    private String placeName ;

    @Column(name = "schedule_time")
    private LocalDateTime scheduleTime ;

    @Column(name = "schedule_end_time")
    private LocalDateTime scheduleEndTime ;

    @Column(name = "memo")
    private String memo ;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CalendarDetails> calendarDetailsList;
}
