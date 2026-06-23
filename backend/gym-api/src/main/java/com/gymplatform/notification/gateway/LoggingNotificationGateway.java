package com.gymplatform.notification.gateway;

import com.gymplatform.notification.domain.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingNotificationGateway implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationGateway.class);

    @Override
    public boolean send(Notification notification) {
        log.info("[{}] notification to member={} gym={} : {}",
                notification.getChannel(), notification.getMemberId(), notification.getGymId(),
                notification.getPayload());
        return true;
    }
}
