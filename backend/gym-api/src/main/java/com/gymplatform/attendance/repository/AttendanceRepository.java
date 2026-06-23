package com.gymplatform.attendance.repository;

import com.gymplatform.attendance.domain.Attendance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    Optional<Attendance> findByMemberIdAndAttendanceDate(UUID memberId, LocalDate attendanceDate);

    List<Attendance> findByMemberIdOrderByAttendanceDateDesc(UUID memberId);

    @Query("""
            SELECT a FROM Attendance a, Member m
            WHERE a.memberId = m.id AND m.gymId = :gymId
              AND a.attendanceDate BETWEEN :startDate AND :endDate
            ORDER BY a.attendanceDate DESC
            """)
    List<Attendance> findByGymIdAndDateRange(@Param("gymId") UUID gymId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT COUNT(a) FROM Attendance a, Member m
            WHERE a.memberId = m.id AND m.gymId = :gymId AND a.attendanceDate = :date
            """)
    long countByGymIdAndDate(@Param("gymId") UUID gymId, @Param("date") LocalDate date);
}
