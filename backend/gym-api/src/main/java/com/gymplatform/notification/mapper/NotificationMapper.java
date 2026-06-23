package com.gymplatform.notification.mapper;

import com.gymplatform.notification.domain.Notification;
import com.gymplatform.notification.dto.NotificationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
