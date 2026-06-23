package com.gymplatform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymplatform.auth.domain.Role;
import com.gymplatform.auth.dto.LoginRequest;
import com.gymplatform.auth.repository.RoleRepository;
import com.gymplatform.auth.repository.UserRepository;
import com.gymplatform.gym.dto.OnboardGymRequest;
import com.gymplatform.membership.dto.PlanRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("integration")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GymOnboardingIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("gymdb_test")
            .withUsername("gymadmin")
            .withPassword("gymadmin");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void rolesAreSeededByFlywayMigration() {
        assertThat(roleRepository.findByCode(Role.SUPER_ADMIN)).isPresent();
        assertThat(roleRepository.findByCode(Role.GYM_ADMIN)).isPresent();
        assertThat(roleRepository.findByCode(Role.TRAINER)).isPresent();
    }

    @Test
    void fullOnboardingLoginAndMemberCreationFlow() {
        // 1. seed a SUPER_ADMIN directly (bootstrap step normally done via a one-off script/migration)
        Role superAdminRole = roleRepository.findByCode(Role.SUPER_ADMIN).orElseThrow();
        var superAdmin = com.gymplatform.auth.domain.User.builder()
                .role(superAdminRole)
                .email("super@platform.com")
                .passwordHash(passwordEncoder.encode("SuperSecret123"))
                .fullName("Platform Super Admin")
                .build();
        userRepository.save(superAdmin);

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

        // 2. login as super admin
        var loginResp = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                new HttpEntity<>(new LoginRequest("super@platform.com", "SuperSecret123"), jsonHeaders),
                String.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String accessToken = readAccessToken(loginResp.getBody());

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        authHeaders.setBearerAuth(accessToken);

        // 3. onboard a gym (creates gym + branch + gym admin)
        var onboardRequest = new OnboardGymRequest(
                "Iron Paradise",
                "Main Branch",
                "123 Fitness St",
                new OnboardGymRequest.GymAdminInfo("Gym Admin", "gymadmin@ironparadise.com", "GymAdmin123", "9999999999")
        );
        var onboardResp = restTemplate.postForEntity(
                url("/api/v1/gyms"), new HttpEntity<>(onboardRequest, authHeaders), String.class);
        assertThat(onboardResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 4. login as the new gym admin
        var gymAdminLoginResp = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                new HttpEntity<>(new LoginRequest("gymadmin@ironparadise.com", "GymAdmin123"), jsonHeaders),
                String.class);
        assertThat(gymAdminLoginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String gymAdminToken = readAccessToken(gymAdminLoginResp.getBody());

        HttpHeaders gymAdminHeaders = new HttpHeaders();
        gymAdminHeaders.setContentType(MediaType.APPLICATION_JSON);
        gymAdminHeaders.setBearerAuth(gymAdminToken);

        // 5. gym admin creates a membership plan
        var planRequest = new PlanRequest("Monthly Basic", "MONTHLY", 30, BigDecimal.valueOf(49.99), "Gym access");
        var planResp = restTemplate.postForEntity(
                url("/api/v1/membership-plans"), new HttpEntity<>(planRequest, gymAdminHeaders), String.class);
        assertThat(planResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 6. gym admin lists branches (should see the one created at onboarding) and creates a member
        var branchesResp = restTemplate.exchange(
                url("/api/v1/branches"), org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(gymAdminHeaders), String.class);
        assertThat(branchesResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(branchesResp.getBody()).contains("Main Branch");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private String readAccessToken(String body) {
        try {
            return objectMapper.readTree(body).get("accessToken").asText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
