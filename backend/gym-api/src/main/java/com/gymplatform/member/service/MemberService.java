package com.gymplatform.member.service;

import com.gymplatform.common.audit.Audited;
import com.gymplatform.common.exception.ResourceNotFoundException;
import com.gymplatform.common.tenancy.TenantContextHolder;
import com.gymplatform.member.domain.Member;
import com.gymplatform.member.domain.MemberDocument;
import com.gymplatform.member.dto.*;
import com.gymplatform.member.mapper.MemberMapper;
import com.gymplatform.member.repository.MemberDocumentRepository;
import com.gymplatform.member.repository.MemberRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberDocumentRepository documentRepository;
    private final MemberMapper memberMapper;

    public MemberService(MemberRepository memberRepository,
                          MemberDocumentRepository documentRepository,
                          MemberMapper memberMapper) {
        this.memberRepository = memberRepository;
        this.documentRepository = documentRepository;
        this.memberMapper = memberMapper;
    }

    @Transactional
    @Audited(action = "CREATE", entityType = "MEMBER")
    public MemberResponse createMember(CreateMemberRequest request) {
        UUID gymId = TenantContextHolder.requireGymId();
        Member member = Member.builder()
                .gymId(gymId)
                .branchId(request.branchId())
                .memberCode(generateMemberCode(gymId))
                .admissionNumber(request.admissionNumber())
                .fullName(request.fullName())
                .phone(request.phone())
                .email(request.email())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .heightCm(request.heightCm())
                .weightKg(request.weightKg())
                .fitnessGoal(request.fitnessGoal())
                .profilePhotoUrl(request.profilePhotoUrl())
                .build();
        return memberMapper.toResponse(memberRepository.save(member));
    }

    @Transactional
    @Audited(action = "UPDATE", entityType = "MEMBER")
    public MemberResponse updateMember(UUID memberId, UpdateMemberRequest request) {
        Member member = findOwnedMemberOrThrow(memberId);

        if (StringUtils.hasText(request.admissionNumber())) {
            member.setAdmissionNumber(request.admissionNumber());
        }
        if (StringUtils.hasText(request.fullName())) {
            member.setFullName(request.fullName());
        }
        if (StringUtils.hasText(request.phone())) {
            member.setPhone(request.phone());
        }
        if (request.email() != null) {
            member.setEmail(request.email());
        }
        if (request.dateOfBirth() != null) {
            member.setDateOfBirth(request.dateOfBirth());
        }
        if (request.gender() != null) {
            member.setGender(request.gender());
        }
        if (request.heightCm() != null) {
            member.setHeightCm(request.heightCm());
        }
        if (request.weightKg() != null) {
            member.setWeightKg(request.weightKg());
        }
        if (request.fitnessGoal() != null) {
            member.setFitnessGoal(request.fitnessGoal());
        }
        if (request.profilePhotoUrl() != null) {
            member.setProfilePhotoUrl(request.profilePhotoUrl());
        }
        return memberMapper.toResponse(memberRepository.save(member));
    }

    public MemberResponse getMember(UUID memberId) {
        return memberMapper.toResponse(findOwnedMemberOrThrow(memberId));
    }

    public Page<MemberResponse> searchMembers(UUID branchId, String search, Pageable pageable) {
        UUID gymId = TenantContextHolder.requireGymId();
        return memberRepository.search(gymId, branchId, search, pageable).map(memberMapper::toResponse);
    }

    @Transactional
    @Audited(action = "DELETE", entityType = "MEMBER")
    public MemberResponse deactivateMember(UUID memberId) {
        Member member = findOwnedMemberOrThrow(memberId);
        member.setStatus(Member.INACTIVE);
        return memberMapper.toResponse(memberRepository.save(member));
    }

    @Transactional
    @Audited(action = "CREATE", entityType = "MEMBER_DOCUMENT")
    public MemberDocumentResponse addDocument(UUID memberId, String docType, String fileUrl) {
        findOwnedMemberOrThrow(memberId);
        MemberDocument document = MemberDocument.builder()
                .memberId(memberId)
                .docType(docType)
                .fileUrl(fileUrl)
                .build();
        return memberMapper.toResponse(documentRepository.save(document));
    }

    public List<MemberDocumentResponse> listDocuments(UUID memberId) {
        findOwnedMemberOrThrow(memberId);
        return documentRepository.findByMemberId(memberId).stream().map(memberMapper::toResponse).toList();
    }

    private Member findOwnedMemberOrThrow(UUID memberId) {
        UUID gymId = TenantContextHolder.requireGymId();
        return memberRepository.findByIdAndGymId(memberId, gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));
    }

    private String generateMemberCode(UUID gymId) {
        long sequence = memberRepository.countByGymId(gymId) + 1;
        return String.format("M-%05d", sequence);
    }
}
