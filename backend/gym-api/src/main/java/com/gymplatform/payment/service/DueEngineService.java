package com.gymplatform.payment.service;

import com.gymplatform.membership.domain.Membership;
import com.gymplatform.payment.domain.Payment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Smart Due Engine: a pure function of (membership, payments, today). No clock reads, no DB
 * calls — safe to call on every read and from a nightly cache-refresh job alike. See
 * docs/ARCHITECTURE.md §2.3.
 */
@Service
public class DueEngineService {

    private static final long DUE_SOON_WINDOW_DAYS = 7;

    public DueComputation compute(Membership membership, List<Payment> payments, LocalDate today) {
        BigDecimal pendingAmount = computePendingAmount(membership, payments);
        LocalDate endDate = membership.getEndDate();

        if (Membership.RENEWED.equals(membership.getStatus()) || Membership.CANCELLED.equals(membership.getStatus())) {
            return new DueComputation(membership.getStatus(), pendingAmount, null, null, null, endDate);
        }

        boolean hasBalance = pendingAmount.signum() > 0;
        boolean pastEndDate = today.isAfter(endDate);

        if (pastEndDate) {
            long overdueDays = ChronoUnit.DAYS.between(endDate, today);
            String status = hasBalance ? Membership.OVERDUE : Membership.EXPIRED;
            LocalDate nextDueDate = hasBalance ? endDate : null;
            return new DueComputation(status, pendingAmount, nextDueDate, null, overdueDays, endDate);
        }

        long remainingDays = ChronoUnit.DAYS.between(today, endDate);
        String status = remainingDays <= DUE_SOON_WINDOW_DAYS ? Membership.DUE_SOON : Membership.ACTIVE;
        LocalDate nextDueDate = hasBalance ? endDate : null;
        return new DueComputation(status, pendingAmount, nextDueDate, remainingDays, null, endDate);
    }

    private BigDecimal computePendingAmount(Membership membership, List<Payment> payments) {
        BigDecimal totalPaid = payments.stream()
                .filter(payment -> !payment.isReversed())
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pending = membership.getTotalPrice().subtract(totalPaid);
        return pending.max(BigDecimal.ZERO);
    }
}
