package com.gymplatform.common.security;

import com.gymplatform.common.tenancy.TenantContext;
import com.gymplatform.common.tenancy.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates each request from the Supabase access token in the Authorization header.
 * The token is verified against Supabase's JWKS (see {@code supabaseJwtDecoder}); the
 * {@code email} claim is then used to look up the local domain user, which supplies the
 * gym/branch/role (tenant context). Supabase owns identity & passwords; this app owns
 * tenancy & authorization.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtDecoder jwtDecoder, UserDetailsService userDetailsService) {
        this.jwtDecoder = jwtDecoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            resolveAuthentication(request);
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private void resolveAuthentication(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);
            String email = jwt.getClaimAsString("email");
            if (!StringUtils.hasText(email)) {
                return;
            }
            UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(email);

            var authToken = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            TenantContextHolder.set(new TenantContext(
                    principal.getId(),
                    principal.getGymId(),
                    principal.getBranchId(),
                    principal.getRoleCode()
            ));
        } catch (JwtException | UsernameNotFoundException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
