package com.gymplatform.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymplatform.common.audit.Audited;
import com.gymplatform.common.tenancy.TenantContextHolder;
import com.gymplatform.notification.domain.Notification;
import com.gymplatform.notification.dto.BroadcastRequest;
import com.gymplatform.notification.dto.NotificationResponse;
import com.gymplatform.notification.gateway.NotificationGateway;
import com.gymplatform.notification.mapper.NotificationMapper;
import com.gymplatform.notification.repository.NotificationRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationGateway notificationGateway;
    private final NotificationMapper notificationMapper;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationRepository notificationRepository,
                                NotificationGateway notificationGateway,
                                NotificationMapper notificationMapper,
                                ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationGateway = notificationGateway;
        this.notificationMapper = notificationMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public NotificationResponse send(UUID gymId, UUID memberId, String type, String channel,
                                      Map<String, Object> payload) {
        Notification notification = Notification.builder()
                .gymId(gymId)
                .memberId(memberId)
                .type(type)
                .channel(channel)
                .payload(toJson(payload))
                .build();

        boolean delivered = notificationGateway.send(notification);
        notification.setStatus(delivered ? Notification.SENT : Notification.FAILED);
        notification.setSentAt(delivered ? Instant.now() : null);

        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    @Audited(action = "CREATE", entityType = "NOTIFICATION")
    public NotificationResponse broadcast(BroadcastRequest request) {
        UUID gymId = TenantContextHolder.requireGymId();
        String channel = StringUtils.hasText(request.channel()) ? request.channel() : Notification.IN_APP;
        return send(gymId, null, Notification.PROMOTIONAL, channel, Map.of("message", request.message()));
    }

    public boolean wasNotifiedToday(UUID memberId, String type) {
        Instant midnight = Instant.now().truncatedTo(ChronoUnit.DAYS);
        return notificationRepository.existsByMemberIdAndTypeAndCreatedAtAfter(memberId, type, midnight);
    }

    public List<NotificationResponse> listForGym() {
        UUID gymId = TenantContextHolder.requireGymId();
        return notificationRepository.findByGymIdOrderByCreatedAtDesc(gymId).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    public List<NotificationResponse> listForMember(UUID memberId) {
        return notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize notification payload, storing empty object", e);
            return "{}";
        }
    }
}
