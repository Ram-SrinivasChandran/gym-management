package com.gymplatform.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gymplatform.auth.domain.Role;
import com.gymplatform.auth.domain.User;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setAccessTokenTtlMinutes(15);
        properties.setRefreshTokenTtlDays(7);
        jwtService = new JwtService("test-secret-key-at-least-32-bytes-long!!", properties);

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode(Role.GYM_ADMIN);

        User user = User.builder()
                .id(UUID.randomUUID())
                .gymId(UUID.randomUUID())
                .branchId(UUID.randomUUID())
                .role(role)
                .email("admin@gym.com")
                .passwordHash("hash")
                .fullName("Admin")
                .status(User.ACTIVE)
                .build();
        principal = new UserPrincipal(user);
    }

    @Test
    void generatesTokenWithExpectedClaims() {
        String token = jwtService.generateAccessToken(principal);

        var claims = jwtService.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo(principal.getId().toString());
        assertThat(claims.get("email", String.class)).isEqualTo("admin@gym.com");
        assertThat(claims.get("role", String.class)).isEqualTo(Role.GYM_ADMIN);
        assertThat(claims.get("gymId", String.class)).isEqualTo(principal.getGymId().toString());
    }

    @Test
    void extractsUserIdFromToken() {
        String token = jwtService.generateAccessToken(principal);

        assertThat(jwtService.extractUserId(token)).isEqualTo(principal.getId());
    }

    @Test
    void freshTokenIsNotExpired() {
        String token = jwtService.generateAccessToken(principal);

        assertThat(jwtService.isExpired(token)).isFalse();
    }

    @Test
    void malformedTokenIsTreatedAsExpired() {
        assertThat(jwtService.isExpired("not-a-real-token")).isTrue();
    }

    @Test
    void tamperedTokenFailsToParse() {
        String token = jwtService.generateAccessToken(principal);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtService.parseClaims(tampered)).isInstanceOf(Exception.class);
    }
}
