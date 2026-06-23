package com.gymplatform.payment.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Output of {@link DueEngineService#compute}. Entirely derived — never persisted as the
 * source of truth (see {@code dues} table note in docs/DATABASE.md: it is a read-optimization
 * cache rebuildable from this computation at any time).
 */
public record DueComputation(
        String status,
        BigDecimal pendingAmount,
        LocalDate nextDueDate,
        Long remainingDays,
        Long overdueDays,
        LocalDate membershipExpiryDate
) {
}
