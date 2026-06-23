package com.gymplatform.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gymplatform.gym.domain.Branch;
import com.gymplatform.gym.domain.Gym;
import com.gymplatform.gym.repository.BranchRepository;
import com.gymplatform.gym.repository.GymRepository;
import com.gymplatform.member.domain.Member;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Exercises MemberRepository.search() against a real PostgreSQL instance. A plain Mockito unit
 * test cannot catch this class of bug — see the regression test below, which previously failed
 * with "function lower(bytea) does not exist" when the search parameter was a genuine Java null
 * (as opposed to an empty string) inside the JPQL LOWER(CONCAT(...)) expression.
 */
@Tag("integration")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class MemberRepositoryIT {

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

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GymRepository gymRepository;

    @Autowired
    private BranchRepository branchRepository;

    private UUID newGym() {
        return gymRepository.save(Gym.builder().name("Test Gym " + UUID.randomUUID()).build()).getId();
    }

    private UUID newBranch(UUID gymId) {
        return branchRepository.save(Branch.builder().gymId(gymId).name("Branch " + UUID.randomUUID()).build()).getId();
    }

    @Test
    void searchWithNullQueryReturnsAllMembersForTheGym() {
        UUID gymId = newGym();
        UUID branchId = newBranch(gymId);
        memberRepository.save(member(gymId, branchId, "M-00001", "Alice Smith", "5551112222"));
        memberRepository.save(member(gymId, branchId, "M-00002", "Bob Jones", "5553334444"));

        var page = memberRepository.search(gymId, null, null, PageRequest.of(0, 20));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void searchWithNullBranchAndNonNullQueryFiltersByName() {
        UUID gymId = newGym();
        UUID branchId = newBranch(gymId);
        memberRepository.save(member(gymId, branchId, "M-00003", "Carla Diaz", "5555556666"));
        memberRepository.save(member(gymId, branchId, "M-00004", "David Lee", "5557778888"));

        var page = memberRepository.search(gymId, null, "carla", PageRequest.of(0, 20));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getFullName()).isEqualTo("Carla Diaz");
    }

    @Test
    void searchScopesResultsToTheGivenBranch() {
        UUID gymId = newGym();
        UUID branchA = newBranch(gymId);
        UUID branchB = newBranch(gymId);
        memberRepository.save(member(gymId, branchA, "M-00005", "Branch A Member", "5550001111"));
        memberRepository.save(member(gymId, branchB, "M-00006", "Branch B Member", "5552223333"));

        var page = memberRepository.search(gymId, branchA, null, PageRequest.of(0, 20));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getFullName()).isEqualTo("Branch A Member");
    }

    private Member member(UUID gymId, UUID branchId, String code, String fullName, String phone) {
        return Member.builder()
                .gymId(gymId)
                .branchId(branchId)
                .memberCode(code)
                .fullName(fullName)
                .phone(phone)
                .build();
    }
}
