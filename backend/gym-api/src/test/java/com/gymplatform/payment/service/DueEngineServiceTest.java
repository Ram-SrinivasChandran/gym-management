package com.gymplatform.payment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gymplatform.membership.domain.Membership;
import com.gymplatform.payment.domain.Payment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DueEngineServiceTest {

    private final DueEngineService engine = new DueEngineService();
    private final LocalDate today = LocalDate.of(2026, 6, 20);

    private Membership membership(LocalDate start, LocalDate end, double totalPrice, String status) {
        return Membership.builder()
                .id(UUID.randomUUID())
                .memberId(UUID.randomUUID())
                .planId(UUID.randomUUID())
                .startDate(start)
                .endDate(end)
                .totalPrice(BigDecimal.valueOf(totalPrice))
                .status(status)
                .build();
    }

    private Payment payment(double amount, boolean reversed) {
        return Payment.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(amount))
                .reversed(reversed)
                .build();
    }

    @Test
    void fullyPaidWithPlentyOfTimeRemainingIsActive() {
        Membership m = membership(today.minusDays(20), today.plusDays(10), 100, Membership.ACTIVE);
        DueComputation result = engine.compute(m, List.of(payment(100, false)), today);

        assertThat(result.status()).isEqualTo(Membership.ACTIVE);
        assertThat(result.pendingAmount()).isEqualByComparingTo("0");
        assertThat(result.nextDueDate()).isNull();
        assertThat(result.remainingDays()).isEqualTo(10);
        assertThat(result.overdueDays()).isNull();
        assertThat(result.membershipExpiryDate()).isEqualTo(m.getEndDate());
    }

    @Test
    void partiallyPaidWithPlentyOfTimeRemainingIsStillActiveButHasNextDueDate() {
        Membership m = membership(today.minusDays(20), today.plusDays(10), 100, Membership.ACTIVE);
        DueComputation result = engine.compute(m, List.of(payment(40, false)), today);

        assertThat(result.status()).isEqualTo(Membership.ACTIVE);
        assertThat(result.pendingAmount()).isEqualByComparingTo("60");
        assertThat(result.nextDueDate()).isEqualTo(m.getEndDate());
        assertThat(result.remainingDays()).isEqualTo(10);
    }

    @Test
    void fullyPaidWithinDueSoonWindowIsDueSoonWithNoNextDueDate() {
        Membership m = membership(today.minusDays(23), today.plusDays(7), 100, Membership.ACTIVE);
        DueComputation result = engine.compute(m, List.of(payment(100, false)), today);

        assertThat(result.status()).isEqualTo(Membership.DUE_SOON);
        assertThat(result.nextDueDate()).isNull();
        assertThat(result.remainingDays()).isEqualTo(7);
    }

    @Test
    void unpaidWithinDueSoonWindowIsDueSoonWithNextDueDate() {
        Membership m = membership(today.minusDays(23), today.plusDays(7), 100, Membership.ACTIVE);
        DueComputation result = engine.compute(m, List.of(), today);

        assertThat(result.status()).isEqualTo(Membership.DUE_SOON);
        assertThat(result.nextDueDate()).isEqualTo(m.getEndDate());
        assertThat(result.pendingAmount()).isEqualByComparingTo("100");
    }

    @Test
    void exactlyEightDaysRemainingIsActiveNotDueSoon() {
        Membership m = membership(today.minusDays(22), today.plusDays(8), 100, Membership.ACTIVE);
        DueComputation result = engine.compute(m, List.of(payment(100, false)), today);

        assertThat(result.status()).isEqualTo(Membership.ACTIVE);
    }

    @Test
    void todayEqualToEndDateIsDueSoonBoundary() {
        Membership m = membership(today.minusDays(30), today, 100, Membership.ACTIVE);
        DueComputation result = engine.compute(m, List.of(payment(100, false)), today);

        assertThat(result.status()).isEqualTo(Membership.DUE_SOON);
        assertThat(result.remainingDays()).isZero();
    }

    @Test
    void pastEndDateWithOutstandingBalanceIsOverdue() {
        Membership m = membership(today.minusDays(40), today.minusDays(5), 100, Membership.ACTIVE);
        DueComputation result = engine.compute(m, List.of(payment(40, false)), today);

        assertThat(result.status()).isEqualTo(Membership.OVERDUE);
        assertThat(result.pendingAmount()).isEqualByComparingTo("60");
        assertThat(result.nextDueDate()).isEqualTo(m.getEndDate());
        assertThat(result.overdueDays()).isEqualTo(5);
        assertThat(result.remainingDays()).isNull();
    }

    @Test
    void pastEndDateFullyPaidIsExpired() {
        Membership m = membership(today.minusDays(40), today.minusDays(5), 100, Membership.ACTIVE);
        DueComputation result = engine.compute(m, List.of(payment(100, false)), today);

        assertThat(result.status()).isEqualTo(Membership.EXPIRED);
        assertThat(result.pendingAmount()).isEqualByComparingTo("0");
        assertThat(result.nextDueDate()).isNull();
        assertThat(result.overdueDays()).isEqualTo(5);
    }

    @Test
    void renewedMembershipStatusPassesThroughUnchanged() {
        Membership m = membership(today.minusDays(40), today.minusDays(5), 100, Membership.RENEWED);
        DueComputation result = engine.compute(m, List.of(payment(100, false)), today);

        assertThat(result.status()).isEqualTo(Membership.RENEWED);
        assertThat(result.nextDueDate()).isNull();
        assertThat(result.remainingDays()).isNull();
        assertThat(result.overdueDays()).isNull();
        assertThat(result.pendingAmount()).isEqualByComparingTo("0");
    }

    @Test
    void cancelledMembershipStatusPassesThroughUnchanged() {
        Membership m = membership(today.minusDays(10), today.plusDays(20), 100, Membership.CANCELLED);
        DueComputation result = engine.compute(m, List.of(), today);

        assertThat(result.status()).isEqualTo(Membership.CANCELLED);
        assertThat(result.nextDueDate()).isNull();
    }

    @Test
    void reversedPaymentsAreExcludedFromPendingAmountCalculation() {
        Membership m = membership(today.minusDays(10), today.plusDays(20), 100, Membership.ACTIVE);
        DueComputation result = engine.compute(m, List.of(payment(100, true)), today);

        assertThat(result.pendingAmount()).isEqualByComparingTo("100");
    }

    @Test
    void overpaymentClampsPendingAmountToZeroNotNegative() {
        Membership m = membership(today.minusDays(10), today.plusDays(20), 100, Membership.ACTIVE);
        DueComputation result = engine.compute(m, List.of(payment(60, false), payment(60, false)), today);

        assertThat(result.pendingAmount()).isEqualByComparingTo("0");
    }

    @Test
    void noPaymentsAtAllTreatedAsFullyOutstanding() {
        Membership m = membership(today.minusDays(10), today.plusDays(20), 100, Membership.ACTIVE);
        DueComputation result = engine.compute(m, List.of(), today);

        assertThat(result.pendingAmount()).isEqualByComparingTo("100");
    }
}
