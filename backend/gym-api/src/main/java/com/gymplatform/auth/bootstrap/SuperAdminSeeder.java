package com.gymplatform.auth.bootstrap;

import com.gymplatform.auth.domain.Role;
import com.gymplatform.auth.domain.User;
import com.gymplatform.auth.repository.RoleRepository;
import com.gymplatform.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Opt-in bootstrap for a single SUPER_ADMIN account, since there is intentionally no public
 * registration endpoint for that role. Activated only via the {@code seed} Spring profile
 * (set in docker-compose.yml for local/E2E use) — never enabled in the production deployment.
 */
@Component
@Profile("seed")
public class SuperAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public SuperAdminSeeder(UserRepository userRepository,
                             RoleRepository roleRepository,
                             PasswordEncoder passwordEncoder,
                             @Value("${gymplatform.seed.super-admin-email:superadmin@platform.local}") String email,
                             @Value("${gymplatform.seed.super-admin-password:SuperAdmin123!}") String password) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            log.info("Seed super admin already exists: {}", email);
            return;
        }

        Role superAdminRole = roleRepository.findByCode(Role.SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role not seeded by Flyway"));

        User superAdmin = User.builder()
                .role(superAdminRole)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName("Platform Super Admin")
                .build();
        userRepository.save(superAdmin);
        log.info("Seeded super admin account: {}", email);
    }
}
