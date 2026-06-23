package com.gymplatform.common.audit;

import com.gymplatform.common.tenancy.TenantContextHolder;
import java.lang.reflect.Method;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        Object result = joinPoint.proceed();

        UUID actorUserId = null;
        try {
            actorUserId = TenantContextHolder.get().userId();
        } catch (Exception ignored) {
            // no tenant context (e.g. login itself) - audited.actor stays null
        }

        AuditLog log = AuditLog.builder()
                .actorUserId(actorUserId)
                .action(audited.action())
                .entityType(audited.entityType())
                .entityId(extractEntityId(result))
                .ipAddress(extractIp())
                .build();
        auditLogRepository.save(log);

        return result;
    }

    private UUID extractEntityId(Object result) {
        if (result == null) {
            return null;
        }
        try {
            Method getId = result.getClass().getMethod("getId");
            Object id = getId.invoke(result);
            return id instanceof UUID uuid ? uuid : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractIp() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest().getRemoteAddr();
        }
        return null;
    }
}
