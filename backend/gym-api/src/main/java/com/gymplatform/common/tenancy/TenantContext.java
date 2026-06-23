package com.gymplatform.common.tenancy;

import java.util.UUID;

public record TenantContext(UUID userId, UUID gymId, UUID branchId, String role) {

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(role);
    }
}
