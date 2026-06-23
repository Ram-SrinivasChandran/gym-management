package com.gymplatform.gym.service;

import com.gymplatform.common.audit.Audited;
import com.gymplatform.common.exception.ResourceNotFoundException;
import com.gymplatform.common.tenancy.TenantContextHolder;
import com.gymplatform.gym.domain.Branch;
import com.gymplatform.gym.dto.BranchRequest;
import com.gymplatform.gym.dto.BranchResponse;
import com.gymplatform.gym.mapper.BranchMapper;
import com.gymplatform.gym.repository.BranchRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    public BranchService(BranchRepository branchRepository, BranchMapper branchMapper) {
        this.branchRepository = branchRepository;
        this.branchMapper = branchMapper;
    }

    @Transactional
    @Audited(action = "CREATE", entityType = "BRANCH")
    public BranchResponse addBranch(BranchRequest request) {
        UUID gymId = TenantContextHolder.requireGymId();
        Branch branch = Branch.builder()
                .gymId(gymId)
                .name(request.name())
                .address(request.address())
                .build();
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    public List<BranchResponse> listBranches() {
        UUID gymId = TenantContextHolder.requireGymId();
        return branchRepository.findByGymId(gymId).stream().map(branchMapper::toResponse).toList();
    }

    public BranchResponse getBranch(UUID branchId) {
        Branch branch = findOwnedBranchOrThrow(branchId);
        return branchMapper.toResponse(branch);
    }

    private Branch findOwnedBranchOrThrow(UUID branchId) {
        UUID gymId = TenantContextHolder.requireGymId();
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));
        if (!branch.getGymId().equals(gymId)) {
            throw new ResourceNotFoundException("Branch", branchId);
        }
        return branch;
    }
}
