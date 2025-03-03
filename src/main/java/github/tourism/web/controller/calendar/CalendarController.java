package github.tourism.web.controller.calendar;

import github.tourism.data.entity.calendar.Calendar;
import github.tourism.data.entity.map.Map;
import github.tourism.service.calendar.CalendarDetailsService;
import github.tourism.service.calendar.CalendarService;
import github.tourism.web.dto.calendar.CalendarRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/calendar")
public class CalendarController {

    private final CalendarService calendarService;
    private final CalendarDetailsService calendarDetailsService;

    // 단일 또는 여러 개의 캘린더 항목(여행 일정) 등록
    // 단일 캘린더 생성
    @PostMapping("/single")
    public ResponseEntity<Calendar> createSingleCalendar(@RequestBody CalendarRequestDTO calendarRequestDTO) {
        Calendar createdCalendar = calendarService.createSingleCalendar(calendarRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCalendar);
    }

    @PostMapping("/schedule/{userId}")
    public ResponseEntity<Calendar> createSingleCalendar(
            @PathVariable Integer userId,
            @RequestBody CalendarRequestDTO calendarRequestDTO) {
        // 요청 객체에 userId 설정
        calendarRequestDTO.setUserId(userId); // userId 설정

        // 서비스 계층에 요청 처리 위임
        Calendar createdCalendar = calendarService.createSingleCalendar(calendarRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCalendar);
    }

    // 여러 캘린더 생성
    @PostMapping("/multiple")
    public ResponseEntity<List<Calendar>> createMultipleCalendars(@RequestBody List<CalendarRequestDTO> calendarRequestDTOS) {
        List<Calendar> createdCalendars = calendarService.createMultipleCalendars(calendarRequestDTOS);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCalendars);
    }

    //  특정 사용자가 등록한 모든 여행 날짜 반환
    @GetMapping("/dates/{userId}")
    public ResponseEntity<List<LocalDate>> getDistinctDatesByUserId(
            @PathVariable int userId) {
        List<LocalDate> dates = calendarDetailsService.getDistinctTourStartDatesByUserId(userId);
        return dates.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(dates);
    }
}

