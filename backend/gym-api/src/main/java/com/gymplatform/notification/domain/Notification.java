package com.gymplatform.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    public static final String DUE_REMINDER = "DUE_REMINDER";
    public static final String RENEWAL_REMINDER = "RENEWAL_REMINDER";
    public static final String PAYMENT_CONFIRMATION = "PAYMENT_CONFIRMATION";
    public static final String ATTENDANCE_ALERT = "ATTENDANCE_ALERT";
    public static final String PROMOTIONAL = "PROMOTIONAL";

    public static final String PUSH = "PUSH";
    public static final String SMS = "SMS";
    public static final String EMAIL = "EMAIL";
    public static final String IN_APP = "IN_APP";

    public static final String PENDING = "PENDING";
    public static final String SENT = "SENT";
    public static final String FAILED = "FAILED";

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "gym_id", nullable = false)
    private UUID gymId;

    @Column(name = "member_id")
    private UUID memberId;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 20)
    private String channel;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(name = "sent_at")
    private Instant sentAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
