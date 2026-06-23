package com.gymplatform.report.api;

import com.gymplatform.payment.dto.PaymentResponse;
import com.gymplatform.report.dto.AttendanceReportResponse;
import com.gymplatform.report.dto.MembershipReportResponse;
import com.gymplatform.report.dto.RevenueReportResponse;
import com.gymplatform.report.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports")
@PreAuthorize("hasRole('GYM_ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/revenue")
    public RevenueReportResponse revenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportService.getRevenueReport(startDate, endDate);
    }

    @GetMapping("/payments")
    public List<PaymentResponse> paymentReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportService.getPaymentReport(startDate, endDate);
    }

    @GetMapping("/memberships")
    public MembershipReportResponse membershipReport() {
        return reportService.getMembershipReport();
    }

    @GetMapping("/attendance")
    public AttendanceReportResponse attendanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportService.getAttendanceReport(startDate, endDate);
    }

    @GetMapping("/revenue/export/excel")
    public ResponseEntity<byte[]> revenueExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        byte[] bytes = reportService.exportRevenueReportExcel(startDate, endDate);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=revenue-report.xlsx")
                .body(bytes);
    }

    @GetMapping("/revenue/export/pdf")
    public ResponseEntity<byte[]> revenuePdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        byte[] bytes = reportService.exportRevenueReportPdf(startDate, endDate);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=revenue-report.pdf")
                .body(bytes);
    }
}
