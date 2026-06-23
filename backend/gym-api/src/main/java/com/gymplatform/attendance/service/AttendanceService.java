package com.gymplatform.attendance.service;

import com.gymplatform.attendance.domain.Attendance;
import com.gymplatform.attendance.dto.AttendanceResponse;
import com.gymplatform.attendance.dto.CheckInRequest;
import com.gymplatform.attendance.mapper.AttendanceMapper;
import com.gymplatform.attendance.repository.AttendanceRepository;
import com.gymplatform.common.audit.Audited;
import com.gymplatform.common.exception.ConflictException;
import com.gymplatform.common.exception.ResourceNotFoundException;
import com.gymplatform.common.tenancy.TenantContextHolder;
import com.gymplatform.member.repository.MemberRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final AttendanceMapper attendanceMapper;

    public AttendanceService(AttendanceRepository attendanceRepository,
                              MemberRepository memberRepository,
                              AttendanceMapper attendanceMapper) {
        this.attendanceRepository = attendanceRepository;
        this.memberRepository = memberRepository;
        this.attendanceMapper = attendanceMapper;
    }

    @Transactional
    @Audited(action = "CREATE", entityType = "ATTENDANCE")
    public AttendanceResponse checkIn(CheckInRequest request) {
        UUID gymId = TenantContextHolder.requireGymId();
        memberRepository.findByIdAndGymId(request.memberId(), gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.memberId()));

        LocalDate today = LocalDate.now();
        attendanceRepository.findByMemberIdAndAttendanceDate(request.memberId(), today)
                .ifPresent(existing -> {
                    throw new ConflictException("Member already checked in today (attendanceId=" + existing.getId() + ")");
                });

        Attendance attendance = Attendance.builder()
                .memberId(request.memberId())
                .attendanceDate(today)
                .checkInAt(Instant.now())
                .method(StringUtils.hasText(request.method()) ? request.method() : Attendance.MANUAL)
                .build();
        return attendanceMapper.toResponse(attendanceRepository.save(attendance));
    }

    @Transactional
    @Audited(action = "UPDATE", entityType = "ATTENDANCE")
    public AttendanceResponse checkOut(UUID memberId) {
        UUID gymId = TenantContextHolder.requireGymId();
        memberRepository.findByIdAndGymId(memberId, gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));

        Attendance attendance = attendanceRepository.findByMemberIdAndAttendanceDate(memberId, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance (today)", memberId));

        if (attendance.getCheckOutAt() != null) {
            throw new ConflictException("Member already checked out today");
        }

        attendance.setCheckOutAt(Instant.now());
        return attendanceMapper.toResponse(attendanceRepository.save(attendance));
    }

    public List<AttendanceResponse> getMemberHistory(UUID memberId) {
        UUID gymId = TenantContextHolder.requireGymId();
        memberRepository.findByIdAndGymId(memberId, gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));

        return attendanceRepository.findByMemberIdOrderByAttendanceDateDesc(memberId).stream()
                .map(attendanceMapper::toResponse)
                .toList();
    }

    public List<AttendanceResponse> getGymAttendanceForRange(LocalDate startDate, LocalDate endDate) {
        UUID gymId = TenantContextHolder.requireGymId();
        return attendanceRepository.findByGymIdAndDateRange(gymId, startDate, endDate).stream()
                .map(attendanceMapper::toResponse)
                .toList();
    }

    public long getDailyCount(LocalDate date) {
        UUID gymId = TenantContextHolder.requireGymId();
        return attendanceRepository.countByGymIdAndDate(gymId, date);
    }
}
