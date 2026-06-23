package com.gymplatform.gym.mapper;

import com.gymplatform.gym.domain.Gym;
import com.gymplatform.gym.dto.GymResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GymMapper {

    GymResponse toResponse(Gym gym);
}
