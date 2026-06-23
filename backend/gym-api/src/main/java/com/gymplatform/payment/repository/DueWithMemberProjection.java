package com.gymplatform.payment.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface DueWithMemberProjection {

    UUID getMembershipId();

    UUID getMemberId();

    UUID getGymId();

    String getCachedStatus();

    LocalDate getNextDueDate();

    BigDecimal getPendingAmount();
}
