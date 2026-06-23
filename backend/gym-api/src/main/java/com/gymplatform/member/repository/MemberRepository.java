package com.gymplatform.member.repository;

import com.gymplatform.member.domain.Member;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    long countByGymId(UUID gymId);

    long countByGymIdAndCreatedAtBetween(UUID gymId, Instant start, Instant end);

    Optional<Member> findByIdAndGymId(UUID id, UUID gymId);

    @Query("""
            SELECT m FROM Member m
            WHERE m.gymId = :gymId
              AND (:branchId IS NULL OR m.branchId = :branchId)
              AND (:search IS NULL
                   OR LOWER(m.fullName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                   OR m.phone LIKE CONCAT('%', CAST(:search AS string), '%')
                   OR LOWER(m.memberCode) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            """)
    Page<Member> search(@Param("gymId") UUID gymId,
                         @Param("branchId") UUID branchId,
                         @Param("search") String search,
                         Pageable pageable);
}
