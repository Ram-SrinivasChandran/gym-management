package com.gymplatform.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.gymplatform.attendance.repository.AttendanceRepository;
import com.gymplatform.common.tenancy.TenantContext;
import com.gymplatform.common.tenancy.TenantContextHolder;
import com.gymplatform.dashboard.dto.DashboardSummaryResponse;
import com.gymplatform.member.repository.MemberRepository;
import com.gymplatform.membership.domain.Membership;
import com.gymplatform.membership.repository.MembershipRepository;
import com.gymplatform.payment.domain.Payment;
import com.gymplatform.payment.repository.DueRepository;
import com.gymplatform.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MembershipRepository membershipRepository;
    @Mock
    private DueRepository dueRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private AttendanceRepository attendanceRepository;

    private DashboardService dashboardService;
    private final UUID gymId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(memberRepository, membershipRepository, dueRepository,
                paymentRepository, attendanceRepository);
        TenantContextHolder.set(new TenantContext(UUID.randomUUID(), gymId, null, "GYM_ADMIN"));
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void aggregatesAllKpisFromUnderlyingRepositories() {
        when(memberRepository.countByGymId(gymId)).thenReturn(120L);
        when(memberRepository.countByGymIdAndCreatedAtBetween(eq(gymId), any(), any())).thenReturn(8L);

        Membership active1 = Membership.builder().id(UUID.randomUUID()).status(Membership.ACTIVE).build();
        Membership active2 = Membership.builder().id(UUID.randomUUID()).status(Membership.ACTIVE).build();
        Membership expired = Membership.builder().id(UUID.randomUUID()).status(Membership.EXPIRED).build();
        when(membershipRepository.findAllByGymId(gymId)).thenReturn(List.of(active1, active2, expired));

        LocalDate today = LocalDate.now();
        when(dueRepository.countByGymIdAndNextDueDateBetween(gymId, today, today)).thenReturn(3L);
        when(dueRepository.countByGymIdAndNextDueDateBetween(gymId, today, today.plusDays(7))).thenReturn(11L);
        when(dueRepository.countByGymIdAndStatus(gymId, Membership.OVERDUE)).thenReturn(5L);

        when(paymentRepository.findByGymIdAndPaidAtBetween(eq(gymId), any(), any())).thenReturn(List.of(
                Payment.builder().id(UUID.randomUUID()).amount(BigDecimal.valueOf(100)).build(),
                Payment.builder().id(UUID.randomUUID()).amount(BigDecimal.valueOf(250)).build()
        ));

        when(membershipRepository.countRenewalsByGymIdAndCreatedAtBetween(eq(gymId), any(), any())).thenReturn(4L);
        when(attendanceRepository.countByGymIdAndDate(gymId, today)).thenReturn(27L);

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.totalMembers()).isEqualTo(120);
        assertThat(summary.newRegistrationsThisMonth()).isEqualTo(8);
        assertThat(summary.membershipsByStatus().get(Membership.ACTIVE)).isEqualTo(2);
        assertThat(summary.membershipsByStatus().get(Membership.EXPIRED)).isEqualTo(1);
        assertThat(summary.dueTodayCount()).isEqualTo(3);
        assertThat(summary.dueThisWeekCount()).isEqualTo(11);
        assertThat(summary.overdueCount()).isEqualTo(5);
        assertThat(summary.revenueThisMonth()).isEqualByComparingTo("350");
        assertThat(summary.paymentCountThisMonth()).isEqualTo(2);
        assertThat(summary.renewalsThisMonth()).isEqualTo(4);
        assertThat(summary.attendanceTodayCount()).isEqualTo(27);
    }

    @Test
    void handlesEmptyGymWithAllZeroCounts() {
        when(memberRepository.countByGymId(gymId)).thenReturn(0L);
        when(memberRepository.countByGymIdAndCreatedAtBetween(eq(gymId), any(), any())).thenReturn(0L);
        when(membershipRepository.findAllByGymId(gymId)).thenReturn(List.of());
        when(dueRepository.countByGymIdAndNextDueDateBetween(any(), any(), any())).thenReturn(0L);
        when(dueRepository.countByGymIdAndStatus(any(), any())).thenReturn(0L);
        when(paymentRepository.findByGymIdAndPaidAtBetween(eq(gymId), any(), any())).thenReturn(List.of());
        when(membershipRepository.countRenewalsByGymIdAndCreatedAtBetween(eq(gymId), any(), any())).thenReturn(0L);
        when(attendanceRepository.countByGymIdAndDate(eq(gymId), any())).thenReturn(0L);

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.totalMembers()).isZero();
        assertThat(summary.membershipsByStatus()).isEmpty();
        assertThat(summary.revenueThisMonth()).isEqualByComparingTo("0");
    }
}
