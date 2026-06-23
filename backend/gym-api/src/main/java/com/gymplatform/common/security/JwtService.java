package com.gymplatform.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final JwtProperties properties;

    public JwtService(@Value("${gymplatform.jwt.secret}") String secret, JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.properties = properties;
    }

    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.getAccessTokenTtlMinutes(), ChronoUnit.MINUTES);

        var builder = Jwts.builder()
                .subject(principal.getId().toString())
                .claim("email", principal.getUsername())
                .claim("role", principal.getRoleCode())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey);

        if (principal.getGymId() != null) {
            builder.claim("gymId", principal.getGymId().toString());
        }
        if (principal.getBranchId() != null) {
            builder.claim("branchId", principal.getBranchId().toString());
        }
        return builder.compact();
    }

    public Instant refreshTokenExpiry() {
        return Instant.now().plus(properties.getRefreshTokenTtlDays(), ChronoUnit.DAYS);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public boolean isExpired(String token) {
        try {
            return parseClaims(token).getExpiration().before(Date.from(Instant.now()));
        } catch (Exception e) {
            return true;
        }
    }
}
