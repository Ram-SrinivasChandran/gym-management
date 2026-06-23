package com.gymplatform.notification.service;

import com.gymplatform.membership.domain.Membership;
import com.gymplatform.notification.domain.Notification;
import com.gymplatform.payment.repository.DueRepository;
import com.gymplatform.payment.repository.DueWithMemberProjection;
import java.util.List;
import java.util.Map;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Daily scan of the dues cache: members whose membership is DUE_SOON get a renewal reminder,
 * members who are OVERDUE get a due reminder. Skips members already notified of that type
 * today so reminders don't spam on every job run.
 */
@Component
public class NotificationTriggerJob {

    private final DueRepository dueRepository;
    private final NotificationService notificationService;

    public NotificationTriggerJob(DueRepository dueRepository, NotificationService notificationService) {
        this.dueRepository = dueRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendDueAndRenewalReminders() {
        List<DueWithMemberProjection> dueSoon = dueRepository.findAllWithMemberByStatusIn(List.of(Membership.DUE_SOON));
        for (DueWithMemberProjection projection : dueSoon) {
            notifyOnce(projection, Notification.RENEWAL_REMINDER);
        }

        List<DueWithMemberProjection> overdue = dueRepository.findAllWithMemberByStatusIn(List.of(Membership.OVERDUE));
        for (DueWithMemberProjection projection : overdue) {
            notifyOnce(projection, Notification.DUE_REMINDER);
        }
    }

    private void notifyOnce(DueWithMemberProjection projection, String type) {
        if (notificationService.wasNotifiedToday(projection.getMemberId(), type)) {
            return;
        }
        notificationService.send(
                projection.getGymId(),
                projection.getMemberId(),
                type,
                Notification.IN_APP,
                Map.of(
                        "membershipId", projection.getMembershipId().toString(),
                        "pendingAmount", projection.getPendingAmount(),
                        "nextDueDate", String.valueOf(projection.getNextDueDate())
                )
        );
    }
}
