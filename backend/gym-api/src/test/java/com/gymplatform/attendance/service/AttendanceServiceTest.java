package com.gymplatform.attendance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.gymplatform.attendance.domain.Attendance;
import com.gymplatform.attendance.dto.AttendanceResponse;
import com.gymplatform.attendance.dto.CheckInRequest;
import com.gymplatform.attendance.mapper.AttendanceMapper;
import com.gymplatform.attendance.repository.AttendanceRepository;
import com.gymplatform.common.exception.ConflictException;
import com.gymplatform.common.exception.ResourceNotFoundException;
import com.gymplatform.common.tenancy.TenantContext;
import com.gymplatform.common.tenancy.TenantContextHolder;
import com.gymplatform.member.domain.Member;
import com.gymplatform.member.repository.MemberRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private MemberRepository memberRepository;

    private AttendanceService attendanceService;

    private final UUID gymId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(attendanceRepository, memberRepository, new AttendanceMapper() {
            @Override
            public AttendanceResponse toResponse(Attendance attendance) {
                return new AttendanceResponse(attendance.getId(), attendance.getMemberId(),
                        attendance.getAttendanceDate(), attendance.getCheckInAt(), attendance.getCheckOutAt(),
                        attendance.getMethod());
            }
        });
        TenantContextHolder.set(new TenantContext(UUID.randomUUID(), gymId, null, "GYM_ADMIN"));
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void checkInCreatesAttendanceRecordForToday() {
        when(memberRepository.findByIdAndGymId(memberId, gymId)).thenReturn(Optional.of(new Member()));
        when(attendanceRepository.findByMemberIdAndAttendanceDate(memberId, LocalDate.now())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance a = invocation.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        AttendanceResponse response = attendanceService.checkIn(new CheckInRequest(memberId, "QR"));

        assertThat(response.memberId()).isEqualTo(memberId);
        assertThat(response.method()).isEqualTo("QR");
        assertThat(response.attendanceDate()).isEqualTo(LocalDate.now());
        assertThat(response.checkOutAt()).isNull();
    }

    @Test
    void checkInDefaultsToManualMethodWhenNotSpecified() {
        when(memberRepository.findByIdAndGymId(memberId, gymId)).thenReturn(Optional.of(new Member()));
        when(attendanceRepository.findByMemberIdAndAttendanceDate(memberId, LocalDate.now())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.checkIn(new CheckInRequest(memberId, null));

        assertThat(response.method()).isEqualTo(Attendance.MANUAL);
    }

    @Test
    void checkInRejectsUnknownMember() {
        when(memberRepository.findByIdAndGymId(memberId, gymId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkIn(new CheckInRequest(memberId, "MANUAL")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void checkInRejectsDuplicateForSameDay() {
        when(memberRepository.findByIdAndGymId(memberId, gymId)).thenReturn(Optional.of(new Member()));
        Attendance existing = Attendance.builder().id(UUID.randomUUID()).memberId(memberId)
                .attendanceDate(LocalDate.now()).checkInAt(Instant.now()).build();
        when(attendanceRepository.findByMemberIdAndAttendanceDate(memberId, LocalDate.now()))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> attendanceService.checkIn(new CheckInRequest(memberId, "MANUAL")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void checkOutSetsCheckOutTimeOnTodaysRecord() {
        when(memberRepository.findByIdAndGymId(memberId, gymId)).thenReturn(Optional.of(new Member()));
        Attendance existing = Attendance.builder().id(UUID.randomUUID()).memberId(memberId)
                .attendanceDate(LocalDate.now()).checkInAt(Instant.now().minusSeconds(3600)).build();
        when(attendanceRepository.findByMemberIdAndAttendanceDate(memberId, LocalDate.now()))
                .thenReturn(Optional.of(existing));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.checkOut(memberId);

        assertThat(response.checkOutAt()).isNotNull();
    }

    @Test
    void checkOutRejectsWhenNoCheckInToday() {
        when(memberRepository.findByIdAndGymId(memberId, gymId)).thenReturn(Optional.of(new Member()));
        when(attendanceRepository.findByMemberIdAndAttendanceDate(memberId, LocalDate.now())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkOut(memberId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void checkOutRejectsDoubleCheckout() {
        when(memberRepository.findByIdAndGymId(memberId, gymId)).thenReturn(Optional.of(new Member()));
        Attendance existing = Attendance.builder().id(UUID.randomUUID()).memberId(memberId)
                .attendanceDate(LocalDate.now()).checkInAt(Instant.now().minusSeconds(3600))
                .checkOutAt(Instant.now()).build();
        when(attendanceRepository.findByMemberIdAndAttendanceDate(memberId, LocalDate.now()))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> attendanceService.checkOut(memberId))
                .isInstanceOf(ConflictException.class);
    }
}
