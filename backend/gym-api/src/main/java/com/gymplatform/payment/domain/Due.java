package com.gymplatform.payment.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Due {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "membership_id", nullable = false, unique = true)
    private UUID membershipId;

    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @Column(name = "pending_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal pendingAmount;

    @Column(name = "cached_status", nullable = false, length = 20)
    private String cachedStatus;

    @Column(name = "last_computed_at", nullable = false)
    private Instant lastComputedAt;
}
