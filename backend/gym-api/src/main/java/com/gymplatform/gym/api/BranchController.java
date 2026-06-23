package com.gymplatform.gym.api;

import com.gymplatform.gym.dto.BranchRequest;
import com.gymplatform.gym.dto.BranchResponse;
import com.gymplatform.gym.service.BranchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/branches")
@Tag(name = "Branches")
@PreAuthorize("hasRole('GYM_ADMIN')")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse addBranch(@Valid @RequestBody BranchRequest request) {
        return branchService.addBranch(request);
    }

    @GetMapping
    public List<BranchResponse> listBranches() {
        return branchService.listBranches();
    }

    @GetMapping("/{branchId}")
    public BranchResponse getBranch(@PathVariable UUID branchId) {
        return branchService.getBranch(branchId);
    }
}
