package com.gymplatform.membership.repository;

import com.gymplatform.membership.domain.Membership;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    List<Membership> findByMemberIdOrderByStartDateDesc(UUID memberId);

    @Query("""
            SELECT mb FROM Membership mb
            WHERE mb.memberId = :memberId AND mb.status NOT IN ('EXPIRED','CANCELLED','RENEWED')
            ORDER BY mb.startDate DESC
            """)
    Optional<Membership> findCurrentByMemberId(@Param("memberId") UUID memberId);

    @Query("""
            SELECT mb FROM Membership mb, Member m
            WHERE m.id = mb.memberId AND m.gymId = :gymId AND mb.id = :membershipId
            """)
    Optional<Membership> findByIdAndGymId(@Param("membershipId") UUID membershipId, @Param("gymId") UUID gymId);

    @Query("SELECT mb FROM Membership mb WHERE mb.status NOT IN ('CANCELLED','RENEWED')")
    List<Membership> findAllNonTerminal();

    @Query("""
            SELECT mb FROM Membership mb, Member m
            WHERE mb.memberId = m.id AND m.gymId = :gymId
            """)
    List<Membership> findAllByGymId(@Param("gymId") UUID gymId);

    @Query("""
            SELECT COUNT(mb) FROM Membership mb, Member m
            WHERE mb.memberId = m.id AND m.gymId = :gymId
              AND mb.renewedFromId IS NOT NULL AND mb.createdAt BETWEEN :start AND :end
            """)
    long countRenewalsByGymIdAndCreatedAtBetween(@Param("gymId") UUID gymId,
                                                  @Param("start") Instant start,
                                                  @Param("end") Instant end);
}
