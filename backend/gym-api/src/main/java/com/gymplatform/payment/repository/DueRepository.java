package com.gymplatform.payment.repository;

import com.gymplatform.payment.domain.Due;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DueRepository extends JpaRepository<Due, UUID> {

    Optional<Due> findByMembershipId(UUID membershipId);

    @Query("""
            SELECT d FROM Due d, Member m, Membership mb
            WHERE mb.id = d.membershipId AND mb.memberId = m.id
              AND m.gymId = :gymId AND d.cachedStatus = :status
            """)
    List<Due> findByGymIdAndStatus(@Param("gymId") UUID gymId, @Param("status") String status);

    @Query("""
            SELECT mb.id AS membershipId, m.id AS memberId, m.gymId AS gymId,
                   d.cachedStatus AS cachedStatus, d.nextDueDate AS nextDueDate, d.pendingAmount AS pendingAmount
            FROM Due d, Member m, Membership mb
            WHERE mb.id = d.membershipId AND mb.memberId = m.id
              AND d.cachedStatus IN :statuses
            """)
    List<DueWithMemberProjection> findAllWithMemberByStatusIn(@Param("statuses") List<String> statuses);

    @Query("""
            SELECT COUNT(d) FROM Due d, Member m, Membership mb
            WHERE mb.id = d.membershipId AND mb.memberId = m.id
              AND m.gymId = :gymId AND d.nextDueDate BETWEEN :start AND :end
            """)
    long countByGymIdAndNextDueDateBetween(@Param("gymId") UUID gymId,
                                            @Param("start") LocalDate start,
                                            @Param("end") LocalDate end);

    @Query("""
            SELECT COUNT(d) FROM Due d, Member m, Membership mb
            WHERE mb.id = d.membershipId AND mb.memberId = m.id
              AND m.gymId = :gymId AND d.cachedStatus = :status
            """)
    long countByGymIdAndStatus(@Param("gymId") UUID gymId, @Param("status") String status);
}
