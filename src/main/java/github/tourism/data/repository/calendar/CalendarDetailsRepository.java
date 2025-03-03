package github.tourism.data.repository.calendar;

import github.tourism.data.entity.calendar.Calendar;
import github.tourism.data.entity.calendar.CalendarDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CalendarDetailsRepository extends JpaRepository<CalendarDetails, Integer> {
    @Query("SELECT cd " +
            "FROM CalendarDetails cd " +
            "JOIN FETCH cd.calendar c " +
            "JOIN FETCH c.favPlace fp " +
            "WHERE fp.user.userId = :userId " +
            "AND c.tourStartDate = :tourStartDate " +
            "AND cd.scheduleTime BETWEEN :startTime AND :endTime")
    List<CalendarDetails> findCalendarDetailsWithFavPlaceByTourStartDate(
            @Param("userId") Integer userId,
            @Param("tourStartDate") LocalDate tourStartDate,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT cd " +
            "FROM CalendarDetails cd " +
            "JOIN FETCH cd.calendar c " +
            "JOIN FETCH c.favPlace fp " +
            "WHERE fp.user.userId = :userId ")
    List<CalendarDetails> findCalendarDetailsWithFavPlace(@Param("userId") Integer userId);

//    @Modifying
//    @Transactional
//    @Query("DELETE " +
//            "FROM CalendarDetails cd " +
//            "WHERE cd.calendar.favPlace.user.userId = :userId " +
//            "AND cd.calendar.calendarId = :calendarId")
//    int deleteCalendarDetailsByUserIdAndCalendarId(
//            @Param("userId") Integer userId,
//            @Param("calendarId") Integer calendarId
//    );




    @Modifying
    @Transactional
    @Query("DELETE FROM CalendarDetails cd " +
            "WHERE cd.calendarDetailsId = :calendarDetailsId " +
            "AND cd.calendar.favPlace.user.userId = :userId ")
    int deleteCalendarDetailsByUserIdAndCalendarDetailsId(
            @Param("userId") Integer userId,
            @Param("calendarDetailsId") Integer calendarDetailsId
    );

    // CalendarDetails에서 Calendar의 calendarId를 참조
    List<CalendarDetails> findByCalendar_CalendarIdInAndScheduleTimeBetween(List<Integer> calendarIds, LocalDateTime startTime, LocalDateTime endTime);

    List<CalendarDetails> findByCalendar_CalendarIdAndScheduleTimeBetween(int calendarId, LocalDateTime startTime, LocalDateTime endTime);

    // calendarId에 맞는 모든 상세 데이터 조회
    List<CalendarDetails> findAllByCalendar_CalendarId(int calendarId);

    // 특정 Calendar에 속한 CalendarDetails 개수 반환
    int countByCalendar(Calendar calendar);
}
