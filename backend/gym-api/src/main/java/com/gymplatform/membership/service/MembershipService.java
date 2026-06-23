package com.gymplatform.membership.service;

import com.gymplatform.common.audit.Audited;
import com.gymplatform.common.exception.ConflictException;
import com.gymplatform.common.exception.ResourceNotFoundException;
import com.gymplatform.common.tenancy.TenantContextHolder;
import com.gymplatform.member.repository.MemberRepository;
import com.gymplatform.membership.domain.Membership;
import com.gymplatform.membership.domain.MembershipPlan;
import com.gymplatform.membership.dto.CreateMembershipRequest;
import com.gymplatform.membership.dto.MembershipResponse;
import com.gymplatform.membership.mapper.MembershipMapper;
import com.gymplatform.membership.repository.MembershipPlanRepository;
import com.gymplatform.membership.repository.MembershipRepository;
import com.gymplatform.payment.repository.PaymentRepository;
import com.gymplatform.payment.service.DueCacheService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MembershipPlanRepository planRepository;
    private final MemberRepository memberRepository;
    private final MembershipMapper membershipMapper;
    private final DueCacheService dueCacheService;
    private final PaymentRepository paymentRepository;

    public MembershipService(MembershipRepository membershipRepository,
                              MembershipPlanRepository planRepository,
                              MemberRepository memberRepository,
                              MembershipMapper membershipMapper,
                              DueCacheService dueCacheService,
                              PaymentRepository paymentRepository) {
        this.membershipRepository = membershipRepository;
        this.planRepository = planRepository;
        this.memberRepository = memberRepository;
        this.membershipMapper = membershipMapper;
        this.dueCacheService = dueCacheService;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    @Audited(action = "CREATE", entityType = "MEMBERSHIP")
    public MembershipResponse createMembership(CreateMembershipRequest request) {
        UUID gymId = TenantContextHolder.requireGymId();

        memberRepository.findByIdAndGymId(request.memberId(), gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.memberId()));
        MembershipPlan plan = planRepository.findByIdAndGymId(request.planId(), gymId)
                .orElseThrow(() -> new ResourceNotFoundException("MembershipPlan", request.planId()));

        membershipRepository.findCurrentByMemberId(request.memberId()).ifPresent(existing -> {
            throw new ConflictException(
                    "Member already has an active membership (id=" + existing.getId() + "); renew it instead");
        });

        Membership membership = Membership.builder()
                .memberId(request.memberId())
                .planId(plan.getId())
                .startDate(request.startDate())
                .endDate(request.startDate().plusDays(plan.getDurationDays()))
                .totalPrice(plan.getPrice())
                .build();
        membership = membershipRepository.save(membership);
        dueCacheService.refresh(membership, Collections.emptyList());
        return membershipMapper.toResponse(membership);
    }

    @Transactional
    @Audited(action = "CREATE", entityType = "MEMBERSHIP")
    public MembershipResponse renewMembership(UUID expiringMembershipId, UUID newPlanId) {
        UUID gymId = TenantContextHolder.requireGymId();
        Membership previous = membershipRepository.findByIdAndGymId(expiringMembershipId, gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", expiringMembershipId));
        MembershipPlan plan = planRepository.findByIdAndGymId(newPlanId, gymId)
                .orElseThrow(() -> new ResourceNotFoundException("MembershipPlan", newPlanId));

        Membership renewed = Membership.builder()
                .memberId(previous.getMemberId())
                .planId(plan.getId())
                .renewedFromId(previous.getId())
                .startDate(previous.getEndDate())
                .endDate(previous.getEndDate().plusDays(plan.getDurationDays()))
                .totalPrice(plan.getPrice())
                .build();
        renewed = membershipRepository.save(renewed);

        previous.setStatus(Membership.RENEWED);
        membershipRepository.save(previous);
        dueCacheService.refresh(previous, paymentRepository.findByMembershipIdOrderByPaidAtDesc(previous.getId()));
        dueCacheService.refresh(renewed, Collections.emptyList());

        return membershipMapper.toResponse(renewed);
    }

    public List<MembershipResponse> getMembershipHistory(UUID memberId) {
        return membershipRepository.findByMemberIdOrderByStartDateDesc(memberId).stream()
                .map(membershipMapper::toResponse)
                .toList();
    }

    public MembershipResponse getMembership(UUID membershipId) {
        UUID gymId = TenantContextHolder.requireGymId();
        Membership membership = membershipRepository.findByIdAndGymId(membershipId, gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", membershipId));
        return membershipMapper.toResponse(membership);
    }
}
