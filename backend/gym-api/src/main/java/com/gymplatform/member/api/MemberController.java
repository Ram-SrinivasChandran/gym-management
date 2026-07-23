package com.gymplatform.member.api;

import com.gymplatform.common.exception.BadRequestException;
import com.gymplatform.member.dto.*;
import com.gymplatform.member.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/members")
@Tag(name = "Members")
@PreAuthorize("hasRole('GYM_ADMIN')")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse createMember(@Valid @RequestBody CreateMemberRequest request) {
        return memberService.createMember(request);
    }

    @GetMapping
    public Page<MemberResponse> searchMembers(@RequestParam(required = false) UUID branchId,
                                               @RequestParam(required = false) String search,
                                               Pageable pageable) {
        return memberService.searchMembers(branchId, search, pageable);
    }

    @GetMapping("/{memberId}")
    public MemberResponse getMember(@PathVariable UUID memberId) {
        return memberService.getMember(memberId);
    }

    @PatchMapping("/{memberId}")
    public MemberResponse updateMember(@PathVariable UUID memberId, @Valid @RequestBody UpdateMemberRequest request) {
        return memberService.updateMember(memberId, request);
    }

    @DeleteMapping("/{memberId}")
    public MemberResponse deactivateMember(@PathVariable UUID memberId) {
        return memberService.deactivateMember(memberId);
    }

    @PostMapping("/{memberId}/photo")
    public MemberResponse uploadPhoto(@PathVariable UUID memberId, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("No file provided.");
        }
        try {
            return memberService.uploadPhoto(memberId, file.getBytes(), file.getContentType());
        } catch (IOException ex) {
            throw new BadRequestException("Could not read the uploaded file.");
        }
    }

    @PostMapping("/{memberId}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberDocumentResponse addDocument(@PathVariable UUID memberId,
                                               @RequestParam String docType,
                                               @RequestParam String fileUrl) {
        return memberService.addDocument(memberId, docType, fileUrl);
    }

    @GetMapping("/{memberId}/documents")
    public List<MemberDocumentResponse> listDocuments(@PathVariable UUID memberId) {
        return memberService.listDocuments(memberId);
    }
}
