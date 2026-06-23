package com.gymplatform.notification.repository;

import com.gymplatform.notification.domain.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByGymIdOrderByCreatedAtDesc(UUID gymId);

    List<Notification> findByMemberIdOrderByCreatedAtDesc(UUID memberId);

    boolean existsByMemberIdAndTypeAndCreatedAtAfter(UUID memberId, String type, Instant since);
}
