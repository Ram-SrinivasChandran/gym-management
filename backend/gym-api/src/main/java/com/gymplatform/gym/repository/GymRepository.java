package com.gymplatform.gym.repository;

import com.gymplatform.gym.domain.Gym;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymRepository extends JpaRepository<Gym, UUID> {
}
