package com.gymplatform.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.gymplatform.auth.domain.RefreshToken;
import com.gymplatform.auth.domain.Role;
import com.gymplatform.auth.domain.User;
import com.gymplatform.auth.dto.LoginRequest;
import com.gymplatform.auth.dto.TokenResponse;
import com.gymplatform.auth.repository.RefreshTokenRepository;
import com.gymplatform.auth.repository.UserRepository;
import com.gymplatform.common.exception.UnauthorizedException;
import com.gymplatform.common.security.JwtProperties;
import com.gymplatform.common.security.JwtService;
import com.gymplatform.common.security.UserPrincipal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtService jwtService;

    private AuthService authService;
    private User user;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setAccessTokenTtlMinutes(15);
        properties.setRefreshTokenTtlDays(7);

        authService = new AuthService(authenticationManager, userRepository, refreshTokenRepository,
                jwtService, properties);

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode(Role.GYM_ADMIN);

        user = User.builder()
                .id(UUID.randomUUID())
                .role(role)
                .email("admin@gym.com")
                .passwordHash("hashed")
                .fullName("Admin")
                .status(User.ACTIVE)
                .build();
        principal = new UserPrincipal(user);
    }

    @Test
    void loginIssuesAccessAndRefreshTokenOnValidCredentials() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateAccessToken(principal)).thenReturn("access-token");
        when(jwtService.refreshTokenExpiry()).thenReturn(Instant.now().plusSeconds(3600));

        TokenResponse response = authService.login(new LoginRequest("admin@gym.com", "password123"), "device-1");

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refreshRejectsUnknownToken() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("bogus-token"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refreshRejectsExpiredToken() {
        RefreshToken expired = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash("hash")
                .expiresAt(Instant.now().minusSeconds(60))
                .build();
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh("expired-token"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refreshRotatesTokenAndRevokesOld() {
        RefreshToken stored = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash("hash")
                .deviceInfo("device-1")
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(stored));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any())).thenReturn("new-access-token");
        when(jwtService.refreshTokenExpiry()).thenReturn(Instant.now().plusSeconds(3600));

        TokenResponse response = authService.refresh("raw-refresh-token");

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getRevokedAt()).isNotNull();
    }

    @Test
    void refreshRejectsInactiveUser() {
        user.setStatus(User.LOCKED);
        RefreshToken stored = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash("hash")
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(stored));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refresh("raw-refresh-token"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void logoutRevokesMatchingToken() {
        RefreshToken stored = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash("hash")
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(stored));

        authService.logout("raw-refresh-token");

        assertThat(stored.getRevokedAt()).isNotNull();
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void logoutAllDevicesRevokesAllForUser() {
        authService.logoutAllDevices(user.getId());

        verify(refreshTokenRepository).revokeAllForUser(eq(user.getId()), any(Instant.class));
    }
}
