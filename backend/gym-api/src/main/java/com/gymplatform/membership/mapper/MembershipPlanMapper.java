package com.gymplatform.membership.mapper;

import com.gymplatform.membership.domain.MembershipPlan;
import com.gymplatform.membership.dto.PlanResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MembershipPlanMapper {

    PlanResponse toResponse(MembershipPlan plan);
}
