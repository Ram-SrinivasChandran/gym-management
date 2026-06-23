package com.gymplatform.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.gymplatform.attendance.domain.Attendance;
import com.gymplatform.attendance.repository.AttendanceRepository;
import com.gymplatform.common.tenancy.TenantContext;
import com.gymplatform.common.tenancy.TenantContextHolder;
import com.gymplatform.membership.domain.Membership;
import com.gymplatform.membership.domain.MembershipPlan;
import com.gymplatform.membership.repository.MembershipPlanRepository;
import com.gymplatform.membership.repository.MembershipRepository;
import com.gymplatform.payment.domain.Payment;
import com.gymplatform.payment.mapper.PaymentMapper;
import com.gymplatform.payment.repository.PaymentRepository;
import com.gymplatform.report.dto.MembershipReportResponse;
import com.gymplatform.report.dto.RevenueReportResponse;
import com.gymplatform.report.util.TableExporter;
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
class ReportServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private MembershipRepository membershipRepository;
    @Mock
    private MembershipPlanRepository membershipPlanRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private PaymentMapper paymentMapper;

    private ReportService reportService;
    private final UUID gymId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        reportService = new ReportService(paymentRepository, membershipRepository, membershipPlanRepository,
                attendanceRepository, paymentMapper, new TableExporter());
        TenantContextHolder.set(new TenantContext(UUID.randomUUID(), gymId, null, "GYM_ADMIN"));
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    private Payment payment(double amount, String method) {
        return Payment.builder().id(UUID.randomUUID()).amount(BigDecimal.valueOf(amount)).paymentMethod(method)
                .paymentType(Payment.FULL).receiptNumber("RCPT-" + UUID.randomUUID()).build();
    }

    @Test
    void revenueReportSumsTotalAndGroupsByPaymentMethod() {
        when(paymentRepository.findByGymIdAndPaidAtBetween(eq(gymId), any(), any()))
                .thenReturn(List.of(payment(100, "CASH"), payment(50, "CASH"), payment(75, "UPI")));

        RevenueReportResponse report = reportService.getRevenueReport(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertThat(report.totalRevenue()).isEqualByComparingTo("225");
        assertThat(report.paymentCount()).isEqualTo(3);
        assertThat(report.revenueByPaymentMethod().get("CASH")).isEqualByComparingTo("150");
        assertThat(report.revenueByPaymentMethod().get("UPI")).isEqualByComparingTo("75");
    }

    @Test
    void revenueReportWithNoPaymentsReturnsZeroTotal() {
        when(paymentRepository.findByGymIdAndPaidAtBetween(eq(gymId), any(), any())).thenReturn(List.of());

        RevenueReportResponse report = reportService.getRevenueReport(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertThat(report.totalRevenue()).isEqualByComparingTo("0");
        assertThat(report.paymentCount()).isZero();
    }

    @Test
    void membershipReportGroupsByStatusAndPlanName() {
        UUID planId = UUID.randomUUID();
        Membership active1 = Membership.builder().id(UUID.randomUUID()).planId(planId)
                .status(Membership.ACTIVE).build();
        Membership active2 = Membership.builder().id(UUID.randomUUID()).planId(planId)
                .status(Membership.ACTIVE).build();
        Membership expired = Membership.builder().id(UUID.randomUUID()).planId(UUID.randomUUID())
                .status(Membership.EXPIRED).build();
        when(membershipRepository.findAllByGymId(gymId)).thenReturn(List.of(active1, active2, expired));

        MembershipPlan plan = MembershipPlan.builder().id(planId).name("Monthly Basic").build();
        when(membershipPlanRepository.findByGymId(gymId)).thenReturn(List.of(plan));

        MembershipReportResponse report = reportService.getMembershipReport();

        assertThat(report.totalMemberships()).isEqualTo(3);
        assertThat(report.countByStatus().get(Membership.ACTIVE)).isEqualTo(2);
        assertThat(report.countByStatus().get(Membership.EXPIRED)).isEqualTo(1);
        assertThat(report.countByPlanName().get("Monthly Basic")).isEqualTo(2);
        assertThat(report.countByPlanName().get("Unknown")).isEqualTo(1);
    }

    @Test
    void attendanceReportCountsTotalAndGroupsByDate() {
        LocalDate day1 = LocalDate.of(2026, 6, 1);
        LocalDate day2 = LocalDate.of(2026, 6, 2);
        when(attendanceRepository.findByGymIdAndDateRange(gymId, day1, day2)).thenReturn(List.of(
                Attendance.builder().id(UUID.randomUUID()).attendanceDate(day1).build(),
                Attendance.builder().id(UUID.randomUUID()).attendanceDate(day1).build(),
                Attendance.builder().id(UUID.randomUUID()).attendanceDate(day2).build()
        ));

        var report = reportService.getAttendanceReport(day1, day2);

        assertThat(report.totalCheckIns()).isEqualTo(3);
        assertThat(report.checkInsByDate().get(day1)).isEqualTo(2);
        assertThat(report.checkInsByDate().get(day2)).isEqualTo(1);
    }
}
