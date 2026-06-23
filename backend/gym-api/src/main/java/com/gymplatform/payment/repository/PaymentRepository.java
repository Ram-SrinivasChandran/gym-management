package com.gymplatform.payment.repository;

import com.gymplatform.payment.domain.Payment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByMembershipIdOrderByPaidAtDesc(UUID membershipId);

    long countByMembershipId(UUID membershipId);

    @Query("""
            SELECT p FROM Payment p, Membership mb, Member m
            WHERE p.membershipId = mb.id AND mb.memberId = m.id
              AND m.gymId = :gymId AND p.paidAt BETWEEN :start AND :end
              AND p.reversed = false
            ORDER BY p.paidAt DESC
            """)
    List<Payment> findByGymIdAndPaidAtBetween(@Param("gymId") UUID gymId,
                                               @Param("start") Instant start,
                                               @Param("end") Instant end);
}
