package com.gymplatform.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record RevenueReportResponse(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalRevenue,
        long paymentCount,
        Map<String, BigDecimal> revenueByPaymentMethod
) {
}
