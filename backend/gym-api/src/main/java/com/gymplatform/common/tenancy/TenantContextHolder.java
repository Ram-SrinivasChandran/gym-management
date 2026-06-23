package com.gymplatform.common.tenancy;

import com.gymplatform.common.exception.ForbiddenException;
import java.util.UUID;

public final class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CONTEXT = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void set(TenantContext context) {
        CONTEXT.set(context);
    }

    public static TenantContext get() {
        TenantContext context = CONTEXT.get();
        if (context == null) {
            throw new ForbiddenException("No tenant context available for this request");
        }
        return context;
    }

    public static UUID requireGymId() {
        TenantContext context = get();
        if (context.gymId() == null) {
            throw new ForbiddenException("Operation requires a gym-scoped account");
        }
        return context.gymId();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
