package com.gymplatform.auth.mapper;

import com.gymplatform.auth.domain.User;
import com.gymplatform.auth.dto.StaffResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StaffMapper {

    @Mapping(target = "role", source = "role.code")
    StaffResponse toResponse(User user);
}
