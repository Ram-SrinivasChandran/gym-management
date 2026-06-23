package com.gymplatform.dashboard.service;

import com.gymplatform.attendance.repository.AttendanceRepository;
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
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final DueRepository dueRepository;
    private final PaymentRepository paymentRepository;
    private final AttendanceRepository attendanceRepository;

    public DashboardService(MemberRepository memberRepository,
                             MembershipRepository membershipRepository,
                             DueRepository dueRepository,
                             PaymentRepository paymentRepository,
                             AttendanceRepository attendanceRepository) {
        this.memberRepository = memberRepository;
        this.membershipRepository = membershipRepository;
        this.dueRepository = dueRepository;
        this.paymentRepository = paymentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public DashboardSummaryResponse getSummary() {
        UUID gymId = TenantContextHolder.requireGymId();
        LocalDate today = LocalDate.now();
        ZoneId zone = ZoneId.systemDefault();
        var monthStart = today.withDayOfMonth(1).atStartOfDay(zone).toInstant();
        var now = java.time.Instant.now();

        long totalMembers = memberRepository.countByGymId(gymId);
        long newRegistrations = memberRepository.countByGymIdAndCreatedAtBetween(gymId, monthStart, now);

        List<Membership> memberships = membershipRepository.findAllByGymId(gymId);
        Map<String, Long> byStatus = memberships.stream()
                .collect(Collectors.groupingBy(Membership::getStatus, Collectors.counting()));

        long dueToday = dueRepository.countByGymIdAndNextDueDateBetween(gymId, today, today);
        long dueThisWeek = dueRepository.countByGymIdAndNextDueDateBetween(gymId, today, today.plusDays(7));
        long overdue = dueRepository.countByGymIdAndStatus(gymId, Membership.OVERDUE);

        List<Payment> paymentsThisMonth = paymentRepository.findByGymIdAndPaidAtBetween(gymId, monthStart, now);
        BigDecimal revenueThisMonth = paymentsThisMonth.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long renewalsThisMonth = membershipRepository.countRenewalsByGymIdAndCreatedAtBetween(gymId, monthStart, now);
        long attendanceToday = attendanceRepository.countByGymIdAndDate(gymId, today);

        return new DashboardSummaryResponse(
                totalMembers,
                newRegistrations,
                byStatus,
                dueToday,
                dueThisWeek,
                overdue,
                revenueThisMonth,
                paymentsThisMonth.size(),
                renewalsThisMonth,
                attendanceToday
        );
    }
}
