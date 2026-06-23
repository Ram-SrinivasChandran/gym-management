package com.gymplatform.common.security;

import com.gymplatform.auth.domain.User;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final UUID gymId;
    private final UUID branchId;
    private final String email;
    private final String passwordHash;
    private final String roleCode;
    private final boolean active;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.gymId = user.getGymId();
        this.branchId = user.getBranchId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.roleCode = user.getRole().getCode();
        this.active = user.isActive();
    }

    public UUID getId() {
        return id;
    }

    public UUID getGymId() {
        return gymId;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public String getRoleCode() {
        return roleCode;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleCode));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }
}
