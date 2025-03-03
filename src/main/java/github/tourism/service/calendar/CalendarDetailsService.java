package github.tourism.service.calendar;

import github.tourism.data.entity.calendar.Calendar;
import github.tourism.data.entity.calendar.CalendarDetails;
import github.tourism.data.repository.calendar.CalendarDetailsRepository;
import github.tourism.data.repository.calendar.CalendarRepository;
import github.tourism.web.advice.ErrorCode;
import github.tourism.web.dto.calendar.CalendarDetailsDTO;
import github.tourism.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static github.tourism.web.advice.ErrorCode.USER_NOT_FOUNDED;

@Service
@RequiredArgsConstructor
public class CalendarDetailsService {
    private final CalendarDetailsRepository calendarDetailsRepository;
    private final CalendarRepository calendarRepository;

    // 캘린더 상세 내용 가져오기 : userId & tourStartDate에 기반하여
    public List<CalendarDetailsDTO> getCalendarDetailsByUserAndDate(int userId, LocalDate tourStartDate) {
        final LocalDateTime tourAtStartOfDay = tourStartDate.atStartOfDay();
        List<CalendarDetails> calendarDetailsList = calendarDetailsRepository.findCalendarDetailsWithFavPlaceByTourStartDate(userId, tourStartDate, tourAtStartOfDay, tourAtStartOfDay.plusDays(1));
        return this.convertCalendarDetailsListToDTOList(calendarDetailsList);//calendarDetailsList.stream().map(this::convertCalendarDetailsToDTO).toList();
    }

    // 캘린더 상세 내용 가져오기 : userId 에 기반하여
    public List<CalendarDetailsDTO> getCalendarDetailsByUser(int userId) {
        List<CalendarDetails> calendarDetailsList = calendarDetailsRepository.findCalendarDetailsWithFavPlace(userId);
        return this.convertCalendarDetailsListToDTOList(calendarDetailsList);//calendarDetailsList.stream().map(this::convertCalendarDetailsToDTO).toList();
    }

    // 캘린더 상세내용 지우기 : userId & calendarId 에 기반하여
//    public void deleteCalendarDetailsByUserIdAndCalendarId(int userId, int calendarId) {
//        int affectedRows = calendarDetailsRepository.deleteCalendarDetailsByUserIdAndCalendarId(userId, calendarId);
//        if (affectedRows == 0) {
//            throw new NotFoundException(USER_NOT_FOUNDED);
//        }
//    }

    // 캘린더 상세내용 지우기 : userId & calendarDetailsId 에 기반하여
    @Transactional
    public void deleteCalendarDetailsByUserIdAndCalendarDetailsId(int userId, int calendarDetailsId) {
        // 1.삭제할 CalendarDetails를 먼저 조회
        Optional<CalendarDetails> optionalDetails = calendarDetailsRepository.findById(calendarDetailsId);

        if (optionalDetails.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 일정이 존재하지 않습니다.");
        }

        CalendarDetails calendarDetails = optionalDetails.get();
        Calendar calendar = calendarDetails.getCalendar();

        // 2. CalendarDetails 삭제
        calendarDetailsRepository.delete(calendarDetails);

        // 3. Calendar에 남아 있는 CalendarDetails가 있는지 확인 후 삭제
        if (calendar.getCalendarDetailsList().isEmpty()) {
            System.out.println("Deleting Calendar with ID: " + calendar.getCalendarId());
            calendarRepository.delete(calendar);
        }
    }

    // 특정 사용자가 등록한 모든 여행 날짜를 반환
    public List<LocalDate> getDistinctTourStartDatesByUserId(int userId){
        return calendarRepository.findDistinctTourStartDatesByUserId(userId);
    }


    private List<CalendarDetailsDTO> convertCalendarDetailsListToDTOList(List<CalendarDetails> calendarDetailsList) {
        return calendarDetailsList.stream()
                .map(this::convertCalendarDetailsToDTO)
                .toList();
    }

    private CalendarDetailsDTO convertCalendarDetailsToDTO(CalendarDetails calendarDetails) {
        return new CalendarDetailsDTO(
                calendarDetails.getCalendarDetailsId(),
//                calendarDetails.getPlaceName(),
                calendarDetails.getCalendar().getScheduleTime(),
                calendarDetails.getCalendar().getScheduleEndTime(),
                calendarDetails.getCalendar().getMemo(),
                calendarDetails.getCalendar().getFavPlace().getPlaceName(),
                calendarDetails.getCalendar().getFavPlace().getPlaceImage(),
                calendarDetails.getCalendar().getFavPlace().getPlaceLocation(),
                calendarDetails.getCalendar().getMapId()
        );
    }
}




