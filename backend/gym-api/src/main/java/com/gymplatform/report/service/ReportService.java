package com.gymplatform.report.service;

import com.gymplatform.attendance.domain.Attendance;
import com.gymplatform.attendance.repository.AttendanceRepository;
import com.gymplatform.common.tenancy.TenantContextHolder;
import com.gymplatform.membership.domain.Membership;
import com.gymplatform.membership.domain.MembershipPlan;
import com.gymplatform.membership.repository.MembershipPlanRepository;
import com.gymplatform.membership.repository.MembershipRepository;
import com.gymplatform.payment.domain.Payment;
import com.gymplatform.payment.dto.PaymentResponse;
import com.gymplatform.payment.mapper.PaymentMapper;
import com.gymplatform.payment.repository.PaymentRepository;
import com.gymplatform.report.dto.AttendanceReportResponse;
import com.gymplatform.report.dto.MembershipReportResponse;
import com.gymplatform.report.dto.RevenueReportResponse;
import com.gymplatform.report.util.TableExporter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final PaymentRepository paymentRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentMapper paymentMapper;
    private final TableExporter tableExporter;

    public ReportService(PaymentRepository paymentRepository,
                          MembershipRepository membershipRepository,
                          MembershipPlanRepository membershipPlanRepository,
                          AttendanceRepository attendanceRepository,
                          PaymentMapper paymentMapper,
                          TableExporter tableExporter) {
        this.paymentRepository = paymentRepository;
        this.membershipRepository = membershipRepository;
        this.membershipPlanRepository = membershipPlanRepository;
        this.attendanceRepository = attendanceRepository;
        this.paymentMapper = paymentMapper;
        this.tableExporter = tableExporter;
    }

    public RevenueReportResponse getRevenueReport(LocalDate startDate, LocalDate endDate) {
        List<Payment> payments = fetchPayments(startDate, endDate);

        BigDecimal total = payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, BigDecimal> byMethod = payments.stream()
                .collect(Collectors.groupingBy(Payment::getPaymentMethod,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));

        return new RevenueReportResponse(startDate, endDate, total, payments.size(), byMethod);
    }

    public List<PaymentResponse> getPaymentReport(LocalDate startDate, LocalDate endDate) {
        return fetchPayments(startDate, endDate).stream().map(paymentMapper::toResponse).toList();
    }

    public MembershipReportResponse getMembershipReport() {
        UUID gymId = TenantContextHolder.requireGymId();
        List<Membership> memberships = membershipRepository.findAllByGymId(gymId);

        Map<String, Long> byStatus = memberships.stream()
                .collect(Collectors.groupingBy(Membership::getStatus, Collectors.counting()));

        Map<UUID, String> planNames = membershipPlanRepository.findByGymId(gymId).stream()
                .collect(Collectors.toMap(MembershipPlan::getId, MembershipPlan::getName));
        Map<String, Long> byPlan = memberships.stream()
                .collect(Collectors.groupingBy(
                        mb -> planNames.getOrDefault(mb.getPlanId(), "Unknown"),
                        Collectors.counting()));

        return new MembershipReportResponse(memberships.size(), byStatus, byPlan);
    }

    public AttendanceReportResponse getAttendanceReport(LocalDate startDate, LocalDate endDate) {
        UUID gymId = TenantContextHolder.requireGymId();
        List<Attendance> records = attendanceRepository.findByGymIdAndDateRange(gymId, startDate, endDate);

        Map<LocalDate, Long> byDate = records.stream()
                .collect(Collectors.groupingBy(Attendance::getAttendanceDate, Collectors.counting()));

        return new AttendanceReportResponse(startDate, endDate, records.size(), byDate);
    }

    public byte[] exportRevenueReportExcel(LocalDate startDate, LocalDate endDate) {
        List<Payment> payments = fetchPayments(startDate, endDate);
        List<String> headers = List.of("Receipt Number", "Amount", "Type", "Method", "Paid At");
        List<List<Object>> rows = payments.stream()
                .map(p -> List.<Object>of(p.getReceiptNumber(), p.getAmount(), p.getPaymentType(),
                        p.getPaymentMethod(), p.getPaidAt().atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .toList();
        return tableExporter.toExcel("Revenue Report", headers, rows);
    }

    public byte[] exportRevenueReportPdf(LocalDate startDate, LocalDate endDate) {
        List<Payment> payments = fetchPayments(startDate, endDate);
        List<String> headers = List.of("Receipt Number", "Amount", "Type", "Method", "Paid At");
        List<List<Object>> rows = payments.stream()
                .map(p -> List.<Object>of(p.getReceiptNumber(), p.getAmount(), p.getPaymentType(),
                        p.getPaymentMethod(), p.getPaidAt().atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .toList();
        String title = "Revenue Report (" + startDate + " to " + endDate + ")";
        return tableExporter.toPdf(title, headers, rows);
    }

    private List<Payment> fetchPayments(LocalDate startDate, LocalDate endDate) {
        UUID gymId = TenantContextHolder.requireGymId();
        var start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        var end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        return paymentRepository.findByGymIdAndPaidAtBetween(gymId, start, end);
    }
}
