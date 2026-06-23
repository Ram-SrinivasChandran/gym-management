package com.gymplatform.membership.api;

import com.gymplatform.membership.dto.CreateMembershipRequest;
import com.gymplatform.membership.dto.MembershipResponse;
import com.gymplatform.membership.service.MembershipService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/memberships")
@Tag(name = "Memberships")
@PreAuthorize("hasRole('GYM_ADMIN')")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MembershipResponse createMembership(@Valid @RequestBody CreateMembershipRequest request) {
        return membershipService.createMembership(request);
    }

    @PostMapping("/{membershipId}/renew")
    public MembershipResponse renew(@PathVariable UUID membershipId, @RequestParam UUID newPlanId) {
        return membershipService.renewMembership(membershipId, newPlanId);
    }

    @GetMapping("/{membershipId}")
    public MembershipResponse getMembership(@PathVariable UUID membershipId) {
        return membershipService.getMembership(membershipId);
    }

    @GetMapping("/member/{memberId}/history")
    public List<MembershipResponse> getHistory(@PathVariable UUID memberId) {
        return membershipService.getMembershipHistory(memberId);
    }
}
