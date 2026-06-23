package com.gymplatform.gym.mapper;

import com.gymplatform.gym.domain.Branch;
import com.gymplatform.gym.dto.BranchResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BranchMapper {

    BranchResponse toResponse(Branch branch);
}
