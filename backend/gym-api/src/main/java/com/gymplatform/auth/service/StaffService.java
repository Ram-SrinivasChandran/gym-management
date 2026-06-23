package com.gymplatform.auth.service;

import com.gymplatform.auth.domain.Role;
import com.gymplatform.auth.domain.User;
import com.gymplatform.auth.dto.CreateStaffRequest;
import com.gymplatform.auth.dto.StaffResponse;
import com.gymplatform.auth.mapper.StaffMapper;
import com.gymplatform.auth.repository.RoleRepository;
import com.gymplatform.auth.repository.UserRepository;
import com.gymplatform.common.audit.Audited;
import com.gymplatform.common.exception.ConflictException;
import com.gymplatform.common.exception.ResourceNotFoundException;
import com.gymplatform.common.tenancy.TenantContextHolder;
import com.gymplatform.gym.repository.BranchRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final StaffMapper staffMapper;

    public StaffService(UserRepository userRepository,
                         RoleRepository roleRepository,
                         BranchRepository branchRepository,
                         PasswordEncoder passwordEncoder,
                         StaffMapper staffMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.staffMapper = staffMapper;
    }

    @Transactional
    @Audited(action = "CREATE", entityType = "STAFF_USER")
    public StaffResponse createStaff(CreateStaffRequest request) {
        UUID gymId = TenantContextHolder.requireGymId();

        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("A user with this email already exists: " + request.email());
        }

        var branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.branchId()));
        if (!branch.getGymId().equals(gymId)) {
            throw new ResourceNotFoundException("Branch", request.branchId());
        }

        Role role = roleRepository.findByCode(request.role())
                .orElseThrow(() -> new ResourceNotFoundException("Role", request.role()));

        User staff = User.builder()
                .gymId(gymId)
                .branchId(branch.getId())
                .role(role)
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .phone(request.phone())
                .build();
        return staffMapper.toResponse(userRepository.save(staff));
    }

    public List<StaffResponse> listStaff() {
        UUID gymId = TenantContextHolder.requireGymId();
        return userRepository.findByGymId(gymId).stream().map(staffMapper::toResponse).toList();
    }

    @Transactional
    @Audited(action = "UPDATE", entityType = "STAFF_USER")
    public StaffResponse updateStatus(UUID staffId, String status) {
        UUID gymId = TenantContextHolder.requireGymId();
        User staff = userRepository.findByIdAndGymId(staffId, gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));
        staff.setStatus(status);
        return staffMapper.toResponse(userRepository.save(staff));
    }
}
