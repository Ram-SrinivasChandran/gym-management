package com.gymplatform.membership.api;

import com.gymplatform.membership.dto.PlanRequest;
import com.gymplatform.membership.dto.PlanResponse;
import com.gymplatform.membership.service.MembershipPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/membership-plans")
@Tag(name = "Membership Plans")
@PreAuthorize("hasRole('GYM_ADMIN')")
public class MembershipPlanController {

    private final MembershipPlanService planService;

    public MembershipPlanController(MembershipPlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanResponse createPlan(@Valid @RequestBody PlanRequest request) {
        return planService.createPlan(request);
    }

    @GetMapping
    public List<PlanResponse> listPlans() {
        return planService.listPlans();
    }

    @GetMapping("/{planId}")
    public PlanResponse getPlan(@PathVariable UUID planId) {
        return planService.getPlan(planId);
    }

    @PutMapping("/{planId}")
    public PlanResponse updatePlan(@PathVariable UUID planId, @Valid @RequestBody PlanRequest request) {
        return planService.updatePlan(planId, request);
    }

    @DeleteMapping("/{planId}")
    public PlanResponse deactivatePlan(@PathVariable UUID planId) {
        return planService.deactivatePlan(planId);
    }
}
