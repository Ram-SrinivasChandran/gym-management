package com.gymplatform.membership.service;

import com.gymplatform.common.audit.Audited;
import com.gymplatform.common.exception.ResourceNotFoundException;
import com.gymplatform.common.tenancy.TenantContextHolder;
import com.gymplatform.membership.domain.MembershipPlan;
import com.gymplatform.membership.dto.PlanRequest;
import com.gymplatform.membership.dto.PlanResponse;
import com.gymplatform.membership.mapper.MembershipPlanMapper;
import com.gymplatform.membership.repository.MembershipPlanRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembershipPlanService {

    private final MembershipPlanRepository planRepository;
    private final MembershipPlanMapper planMapper;

    public MembershipPlanService(MembershipPlanRepository planRepository, MembershipPlanMapper planMapper) {
        this.planRepository = planRepository;
        this.planMapper = planMapper;
    }

    @Transactional
    @Audited(action = "CREATE", entityType = "MEMBERSHIP_PLAN")
    public PlanResponse createPlan(PlanRequest request) {
        UUID gymId = TenantContextHolder.requireGymId();
        MembershipPlan plan = MembershipPlan.builder()
                .gymId(gymId)
                .name(request.name())
                .planType(request.planType())
                .durationDays(request.durationDays())
                .price(request.price())
                .discountAmount(discountOrZero(request))
                .benefits(request.benefits())
                .build();
        return planMapper.toResponse(planRepository.save(plan));
    }

    public List<PlanResponse> listPlans() {
        UUID gymId = TenantContextHolder.requireGymId();
        return planRepository.findByGymId(gymId).stream().map(planMapper::toResponse).toList();
    }

    public PlanResponse getPlan(UUID planId) {
        return planMapper.toResponse(findOwnedPlanOrThrow(planId));
    }

    @Transactional
    @Audited(action = "UPDATE", entityType = "MEMBERSHIP_PLAN")
    public PlanResponse updatePlan(UUID planId, PlanRequest request) {
        MembershipPlan plan = findOwnedPlanOrThrow(planId);
        plan.setName(request.name());
        plan.setPlanType(request.planType());
        plan.setDurationDays(request.durationDays());
        plan.setPrice(request.price());
        plan.setDiscountAmount(discountOrZero(request));
        plan.setBenefits(request.benefits());
        return planMapper.toResponse(planRepository.save(plan));
    }

    private static BigDecimal discountOrZero(PlanRequest request) {
        return request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;
    }

    @Transactional
    @Audited(action = "UPDATE", entityType = "MEMBERSHIP_PLAN")
    public PlanResponse deactivatePlan(UUID planId) {
        MembershipPlan plan = findOwnedPlanOrThrow(planId);
        plan.setStatus(MembershipPlan.INACTIVE);
        return planMapper.toResponse(planRepository.save(plan));
    }

    MembershipPlan findOwnedPlanOrThrow(UUID planId) {
        UUID gymId = TenantContextHolder.requireGymId();
        return planRepository.findByIdAndGymId(planId, gymId)
                .orElseThrow(() -> new ResourceNotFoundException("MembershipPlan", planId));
    }
}
