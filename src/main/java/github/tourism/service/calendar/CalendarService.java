package github.tourism.service.calendar;

import github.tourism.data.entity.calendar.Calendar;
import github.tourism.data.entity.calendar.CalendarDetails;
import github.tourism.data.entity.favPlace.FavPlace;
import github.tourism.data.repository.calendar.CalendarDetailsRepository;
import github.tourism.data.repository.calendar.CalendarRepository;
import github.tourism.data.repository.favPlace.FavPlaceRepository;
import github.tourism.web.dto.calendar.CalendarRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private final CalendarRepository calendarRepository;
    private final CalendarDetailsRepository calendarDetailsRepository;
    private final FavPlaceRepository favPlaceRepository;

    //  1. 특정 날짜 범위로 캘린더 항목 조회
    public List<Calendar> getCalendarEntries(Integer userId, LocalDateTime startTime, LocalDateTime endTime) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 null입니다.");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("시작 날짜가 종료 날짜보다 이후입니다.");
        }
        return calendarRepository.findAllByUserIdAndScheduleTimeBetween(userId, startTime, endTime);

    }

    // 단일 캘린더 생성
    @Transactional
    public Calendar createSingleCalendar(CalendarRequestDTO dto) {
        validateCalendarDTO(dto);

        // 중복 일정 체크!
        if (calendarRepository.existsByUserAndTourStartDateAndTime(dto.getUserId(), dto.getTourStartDate(), dto.getScheduleTime())) {
            throw new IllegalArgumentException("이미 해당 시간에 일정이 존재합니다.");
        }

        Calendar calendar = convertToEntity(dto);
        Calendar savedCalendar = calendarRepository.save(calendar);

        //  추가: CalendarDetails 엔티티 생성 및 저장
        CalendarDetails calendarDetails = new CalendarDetails();
        calendarDetails.setCalendar(savedCalendar);  // 캘린더와 연관 관계 설정
//        calendarDetails.setPlaceName(dto.getPlaceName()); // 장소 이름
//        calendarDetails.setPlaceImage(dto.getPlaceImage()); // 장소 이미지
//        calendarDetails.setPlaceLocation(dto.getPlaceLocation()); // 장소 위치
        calendarDetails.setScheduleTime(dto.getScheduleTime()); // 일정 시작 시간
        calendarDetails.setScheduleEndTime(dto.getScheduleEndTime()); // 일정 종료 시간
        calendarDetails.setMemo(dto.getMemo()); // 메모 추가

        //  FavPlace 정보 가져오기 (찜한 장소 정보가 있으면 사용)
        if (dto.getFavPlaceId() != null) {
            Optional<FavPlace> favPlaceOpt = favPlaceRepository.findById(dto.getFavPlaceId());
            favPlaceOpt.ifPresent(favPlace -> {
                calendarDetails.setPlaceName(favPlace.getPlaceName());
                calendarDetails.setPlaceImage(favPlace.getPlaceImage());
                calendarDetails.setPlaceLocation(favPlace.getPlaceLocation());
            });
        } else {
            // 클라이언트가 직접 place_name 등을 보냈다면 사용
            calendarDetails.setPlaceName(dto.getPlaceName());
            calendarDetails.setPlaceImage(dto.getPlaceImage());
            calendarDetails.setPlaceLocation(dto.getPlaceLocation());
        }

        calendarDetailsRepository.save(calendarDetails); //

        return calendarRepository.save(calendar);
    }

    // 여러 캘린더 생성
    @Transactional
    public List<Calendar> createMultipleCalendars(List<CalendarRequestDTO> calendarRequestDTOS) {
        if (calendarRequestDTOS == null || calendarRequestDTOS.isEmpty()) {
            throw new IllegalArgumentException("캘린더 목록이 비어 있습니다.");
        }

        List<Calendar> calendars = calendarRequestDTOS.stream()
                .map(this::convertToEntity)
                .peek(this::validateCalendar)
                .collect(Collectors.toList());

        return calendarRepository.saveAll(calendars);
    }

    // DTO 검증
    // 일정 생성 시 유효성 검증
    private void validateCalendarDTO(CalendarRequestDTO calendarRequestDTO) {
        if (calendarRequestDTO.getUserId() == null || calendarRequestDTO.getTourStartDate() == null ||
                calendarRequestDTO.getScheduleTime() == null || calendarRequestDTO.getScheduleEndTime() == null) {
            throw new IllegalArgumentException("필수 정보가 누락되었습니다.");
        }
        // 시작 시간이 종료 시간보다 이후인지 체크
        if (calendarRequestDTO.getScheduleTime().isAfter(calendarRequestDTO.getScheduleEndTime())) {
            throw new IllegalArgumentException("일정 시작 시간이 종료 시간보다 이후일 수 없습니다.");
        }
    }

    // 캘린더 검증
    private void validateCalendar(Calendar calendar) {
        if (calendar.getUserId() == null || calendar.getTourStartDate() == null ||
                calendar.getScheduleTime() == null || calendar.getScheduleEndTime() == null) {
            throw new IllegalArgumentException("필수 정보가 누락되었습니다.");
        }
        if (calendar.getScheduleTime().isAfter(calendar.getScheduleEndTime())) {
            throw new IllegalArgumentException("일정 시작 시간이 종료 시간보다 이후일 수 없습니다.");
        }
    }

    // DTO를 엔티티로 변환
    private Calendar convertToEntity(CalendarRequestDTO calendarRequestDTO) {
        Calendar calendar = new Calendar();
        calendar.setUserId(calendarRequestDTO.getUserId());
        calendar.setMapId(calendarRequestDTO.getMapId());
        calendar.setTourStartDate(calendarRequestDTO.getTourStartDate());
        calendar.setScheduleTime(calendarRequestDTO.getScheduleTime());
        calendar.setScheduleEndTime(calendarRequestDTO.getScheduleEndTime());
        calendar.setFavPlaceId(calendarRequestDTO.getFavPlaceId());
//        calendar.setPlaceName(calendarRequestDTO.getPlaceName());
        calendar.setMemo(calendarRequestDTO.getMemo());
        return calendar;
    }
}

