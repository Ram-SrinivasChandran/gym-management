package com.gymplatform.membership.repository;

import com.gymplatform.membership.domain.MembershipPlan;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, UUID> {

    List<MembershipPlan> findByGymId(UUID gymId);

    Optional<MembershipPlan> findByIdAndGymId(UUID id, UUID gymId);
}
