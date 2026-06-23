package com.gymplatform.gym.repository;

import com.gymplatform.gym.domain.Branch;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    List<Branch> findByGymId(UUID gymId);
}
