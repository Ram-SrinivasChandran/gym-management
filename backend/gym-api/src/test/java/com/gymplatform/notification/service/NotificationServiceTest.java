package com.gymplatform.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymplatform.common.tenancy.TenantContext;
import com.gymplatform.common.tenancy.TenantContextHolder;
import com.gymplatform.notification.domain.Notification;
import com.gymplatform.notification.dto.BroadcastRequest;
import com.gymplatform.notification.gateway.NotificationGateway;
import com.gymplatform.notification.mapper.NotificationMapper;
import com.gymplatform.notification.repository.NotificationRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationGateway notificationGateway;

    private NotificationService notificationService;

    private final UUID gymId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        NotificationMapper mapper = notification -> new com.gymplatform.notification.dto.NotificationResponse(
                notification.getId(), notification.getGymId(), notification.getMemberId(), notification.getType(),
                notification.getChannel(), notification.getStatus(), notification.getPayload(),
                notification.getSentAt(), notification.getCreatedAt());
        notificationService = new NotificationService(notificationRepository, notificationGateway, mapper,
                new ObjectMapper());
        TenantContextHolder.set(new TenantContext(UUID.randomUUID(), gymId, null, "GYM_ADMIN"));
        lenient().when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void successfulDeliveryMarksNotificationSent() {
        when(notificationGateway.send(any(Notification.class))).thenReturn(true);

        var response = notificationService.send(gymId, memberId, Notification.PAYMENT_CONFIRMATION,
                Notification.IN_APP, Map.of("amount", 100));

        assertThat(response.status()).isEqualTo(Notification.SENT);
        assertThat(response.sentAt()).isNotNull();
    }

    @Test
    void failedDeliveryMarksNotificationFailedWithNoSentAt() {
        when(notificationGateway.send(any(Notification.class))).thenReturn(false);

        var response = notificationService.send(gymId, memberId, Notification.DUE_REMINDER,
                Notification.SMS, Map.of());

        assertThat(response.status()).isEqualTo(Notification.FAILED);
        assertThat(response.sentAt()).isNull();
    }

    @Test
    void broadcastSendsToWholeGymWithNullMemberId() {
        when(notificationGateway.send(any(Notification.class))).thenReturn(true);

        notificationService.broadcast(new BroadcastRequest("50% off this week!", "IN_APP"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getMemberId()).isNull();
        assertThat(captor.getValue().getType()).isEqualTo(Notification.PROMOTIONAL);
        assertThat(captor.getValue().getGymId()).isEqualTo(gymId);
    }

    @Test
    void broadcastDefaultsToInAppChannelWhenNotSpecified() {
        when(notificationGateway.send(any(Notification.class))).thenReturn(true);

        notificationService.broadcast(new BroadcastRequest("Reminder", null));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo(Notification.IN_APP);
    }

    @Test
    void wasNotifiedTodayDelegatesToRepositoryWithMidnightCutoff() {
        when(notificationRepository.existsByMemberIdAndTypeAndCreatedAtAfter(eq(memberId),
                eq(Notification.DUE_REMINDER), any(Instant.class))).thenReturn(true);

        assertThat(notificationService.wasNotifiedToday(memberId, Notification.DUE_REMINDER)).isTrue();
    }
}
