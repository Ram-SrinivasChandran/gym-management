package com.gymplatform.auth.service;

import com.gymplatform.auth.domain.RefreshToken;
import com.gymplatform.auth.domain.User;
import com.gymplatform.auth.dto.LoginRequest;
import com.gymplatform.auth.dto.TokenResponse;
import com.gymplatform.auth.repository.RefreshTokenRepository;
import com.gymplatform.auth.repository.UserRepository;
import com.gymplatform.common.exception.UnauthorizedException;
import com.gymplatform.common.security.JwtProperties;
import com.gymplatform.common.security.JwtService;
import com.gymplatform.common.security.TokenHasher;
import com.gymplatform.common.security.UserPrincipal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(AuthenticationManager authenticationManager,
                        UserRepository userRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        JwtService jwtService,
                        JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String deviceInfo) {
        var authToken = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        var authentication = authenticationManager.authenticate(authToken);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return issueTokenPair(principal, deviceInfo);
    }

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        String hash = TokenHasher.sha256(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!stored.isValid(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (!user.isActive()) {
            throw new UnauthorizedException("User account is not active");
        }

        // rotate: revoke old, issue new
        stored.setRevokedAt(Instant.now());
        refreshTokenRepository.save(stored);

        UserPrincipal principal = new UserPrincipal(user);
        return issueTokenPair(principal, stored.getDeviceInfo());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = TokenHasher.sha256(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void logoutAllDevices(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId, Instant.now());
    }

    private TokenResponse issueTokenPair(UserPrincipal principal, String deviceInfo) {
        String accessToken = jwtService.generateAccessToken(principal);
        String rawRefreshToken = TokenHasher.generateOpaqueToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(principal.getId())
                .tokenHash(TokenHasher.sha256(rawRefreshToken))
                .deviceInfo(deviceInfo)
                .expiresAt(jwtService.refreshTokenExpiry())
                .build();
        refreshTokenRepository.save(refreshToken);

        long expiresInSeconds = jwtProperties.getAccessTokenTtlMinutes() * 60;
        return TokenResponse.of(accessToken, rawRefreshToken, expiresInSeconds);
    }
}
