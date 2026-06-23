package com.gymplatform.notification.gateway;

import com.gymplatform.notification.domain.Notification;

/**
 * Channel-delivery boundary. v1 ships only {@link LoggingNotificationGateway} (logs + marks
 * sent — no real push/SMS/email provider wired up yet). Swap in an FCM/Twilio/SES-backed
 * implementation later without touching {@code NotificationService} call sites.
 */
public interface NotificationGateway {

    boolean send(Notification notification);
}
