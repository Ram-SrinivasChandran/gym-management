package com.gymplatform.attendance.api;

import com.gymplatform.attendance.dto.AttendanceResponse;
import com.gymplatform.attendance.dto.CheckInRequest;
import com.gymplatform.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance")
@PreAuthorize("hasAnyRole('GYM_ADMIN','TRAINER')")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse checkIn(@Valid @RequestBody CheckInRequest request) {
        return attendanceService.checkIn(request);
    }

    @PostMapping("/check-out/{memberId}")
    public AttendanceResponse checkOut(@PathVariable UUID memberId) {
        return attendanceService.checkOut(memberId);
    }

    @GetMapping("/member/{memberId}/history")
    public List<AttendanceResponse> getHistory(@PathVariable UUID memberId) {
        return attendanceService.getMemberHistory(memberId);
    }

    @GetMapping("/report")
    public List<AttendanceResponse> getReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return attendanceService.getGymAttendanceForRange(startDate, endDate);
    }

    @GetMapping("/count")
    public Map<String, Object> getDailyCount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Map.of("date", date, "count", attendanceService.getDailyCount(date));
    }
}
