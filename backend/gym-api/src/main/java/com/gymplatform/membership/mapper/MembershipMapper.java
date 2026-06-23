package com.gymplatform.membership.mapper;

import com.gymplatform.membership.domain.Membership;
import com.gymplatform.membership.dto.MembershipResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MembershipMapper {

    MembershipResponse toResponse(Membership membership);
}
