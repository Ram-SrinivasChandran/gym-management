package com.gymplatform.notification.api;

import com.gymplatform.notification.dto.BroadcastRequest;
import com.gymplatform.notification.dto.NotificationResponse;
import com.gymplatform.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/broadcast")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('GYM_ADMIN')")
    public NotificationResponse broadcast(@Valid @RequestBody BroadcastRequest request) {
        return notificationService.broadcast(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GYM_ADMIN','TRAINER')")
    public List<NotificationResponse> listForGym() {
        return notificationService.listForGym();
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('GYM_ADMIN','TRAINER')")
    public List<NotificationResponse> listForMember(@PathVariable UUID memberId) {
        return notificationService.listForMember(memberId);
    }
}
