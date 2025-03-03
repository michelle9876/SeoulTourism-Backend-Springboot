package github.tourism.web.controller.calendar;

import github.tourism.service.calendar.CalendarDetailsService;
import github.tourism.web.dto.calendar.CalendarDetailsDTO;
import github.tourism.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/calendar/schedule")
public class CalendarDetailsController {
    private final CalendarDetailsService calendarDetailsService;

// userId & tourStartDate를 기반으로 캘린더 세부정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<List<CalendarDetailsDTO>> getSchedulesByDate(
            @PathVariable int userId,
            @RequestParam String tourStartDate) {
        try {
            // 날짜 파싱
            LocalDate startDate = LocalDate.parse(tourStartDate);

            // 해당 날짜에 등록된 일정 조회
            List<CalendarDetailsDTO> calendarDetailsList = calendarDetailsService.getCalendarDetailsByUserAndDate(userId, startDate);
            return calendarDetailsList.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(calendarDetailsList);

        } catch (DateTimeParseException e) {
            // 잘못된 날짜 형식 예외 처리
            return ResponseEntity.badRequest().body(null);
        }
    }

    // userId를 기반으로 캘린더 세부정보 조회
    @GetMapping("/all/{userId}")
    public ResponseEntity<List<CalendarDetailsDTO>> getAllSchedulesByUser(
            @PathVariable int userId) {
        try {
            // 해당 날짜에 등록된 일정 조회
            List<CalendarDetailsDTO> calendarDetailsList = calendarDetailsService.getCalendarDetailsByUser(userId);
            return calendarDetailsList.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(calendarDetailsList);

        } catch (DateTimeParseException e) {
            // 잘못된 날짜 형식 예외 처리
            return ResponseEntity.badRequest().body(null);
        }
    }

// calendarId & userId 기반으로 캘린더 삭제
//    @DeleteMapping("/{calendarId}")
//    public ResponseEntity<String> deleteScheduleByUser(
//            @PathVariable int calendarId,
//            @RequestParam int userId) {
//        try {
//            // 해당 날짜에 등록된 일정 조회
//            try {
//                calendarDetailsService.deleteCalendarDetailsByUserIdAndCalendarId(userId, calendarId);
//            } catch (NotFoundException e) {
//                return ResponseEntity.notFound().build();
//            }
//            return ResponseEntity.ok("Entry deleted successfully");
//        } catch (DateTimeParseException e) {
//            // 잘못된 날짜 형식 예외 처리
//            return ResponseEntity.badRequest().body(null);
//        }
//    }

    // calendarDetailsId & userId 기반으로 캘린더 삭제
    @DeleteMapping("/{calendarDetailsId}")
    public ResponseEntity<String> deleteScheduleByUser(
            @PathVariable int calendarDetailsId,
            @RequestParam int userId) {
        try {
            calendarDetailsService.deleteCalendarDetailsByUserIdAndCalendarDetailsId(userId, calendarDetailsId);
            return ResponseEntity.ok("Entry deleted successfully");

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류로 인해 삭제에 실패했습니다.");
        }
    }

}


