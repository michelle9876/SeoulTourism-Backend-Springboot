package github.tourism.web.dto.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import github.tourism.data.entity.calendar.Calendar;
import github.tourism.data.entity.calendar.CalendarDetails;
import github.tourism.data.entity.favPlace.FavPlace;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값 자동 제외
public class CalendarRequestDTO {

    @NotNull(message = "userId는 필수 값입니다.")
    private Integer userId;

    @NotNull(message = "mapId는 필수 값입니다.")
    private Integer mapId;

    @NotNull(message = "tourStartDate는 필수 값입니다.")
    private LocalDate tourStartDate;

    @NotNull(message = "scheduleTime은 필수 값입니다.")
    private LocalDateTime scheduleTime;

    @NotNull(message = "scheduleEndTime은 필수 값입니다.")
    private LocalDateTime scheduleEndTime;

    private Integer favPlaceId;
    private FavPlace favPlace; //  null이면 응답에서 자동 제외

//    @NotNull(message = "placeName은 필수 값입니다.")
//    private String placeName;

    private String memo;
    private List<CalendarDetails> calendarDetailsList; // null이면 응답에서 자동 제외

    private String placeName;
    private String placeImage;
    private String placeLocation;


    // 생성자
    public CalendarRequestDTO(
            Integer userId, Integer mapId, LocalDate tourStartDate,
            LocalDateTime scheduleTime, LocalDateTime scheduleEndTime,
            Integer favPlaceId, String memo, String placeName,
            String placeImage, String placeLocation) {
        this.userId = userId;
        this.mapId = mapId;
        this.tourStartDate = tourStartDate;
        this.scheduleTime = scheduleTime;
        this.scheduleEndTime = scheduleEndTime;
        this.favPlaceId = favPlaceId;
        this.memo = memo;
        this.placeName = placeName;
        this.placeImage = placeImage;
        this.placeLocation = placeLocation;
    }


}
