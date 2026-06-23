package com.gymplatform.gym.service;

import com.gymplatform.auth.domain.Role;
import com.gymplatform.auth.domain.User;
import com.gymplatform.auth.repository.RoleRepository;
import com.gymplatform.auth.repository.UserRepository;
import com.gymplatform.common.audit.Audited;
import com.gymplatform.common.exception.ConflictException;
import com.gymplatform.common.exception.ResourceNotFoundException;
import com.gymplatform.gym.domain.Branch;
import com.gymplatform.gym.domain.Gym;
import com.gymplatform.gym.dto.GymResponse;
import com.gymplatform.gym.dto.OnboardGymRequest;
import com.gymplatform.gym.mapper.GymMapper;
import com.gymplatform.gym.repository.BranchRepository;
import com.gymplatform.gym.repository.GymRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GymService {

    private final GymRepository gymRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final GymMapper gymMapper;

    public GymService(GymRepository gymRepository,
                       BranchRepository branchRepository,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       GymMapper gymMapper) {
        this.gymRepository = gymRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.gymMapper = gymMapper;
    }

    @Transactional
    @Audited(action = "CREATE", entityType = "GYM")
    public GymResponse onboardGym(OnboardGymRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.admin().email())) {
            throw new ConflictException("A user with this email already exists: " + request.admin().email());
        }

        Gym gym = Gym.builder().name(request.gymName()).build();
        gym = gymRepository.save(gym);

        Branch branch = Branch.builder()
                .gymId(gym.getId())
                .name(request.firstBranchName())
                .address(request.firstBranchAddress())
                .build();
        branch = branchRepository.save(branch);

        Role gymAdminRole = roleRepository.findByCode(Role.GYM_ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException("Role", Role.GYM_ADMIN));

        User admin = User.builder()
                .gymId(gym.getId())
                .branchId(branch.getId())
                .role(gymAdminRole)
                .email(request.admin().email())
                .passwordHash(passwordEncoder.encode(request.admin().password()))
                .fullName(request.admin().fullName())
                .phone(request.admin().phone())
                .build();
        userRepository.save(admin);

        return gymMapper.toResponse(gym);
    }

    public GymResponse getGym(UUID gymId) {
        return gymMapper.toResponse(findGymOrThrow(gymId));
    }

    public List<GymResponse> listGyms() {
        return gymRepository.findAll().stream().map(gymMapper::toResponse).toList();
    }

    @Transactional
    @Audited(action = "UPDATE", entityType = "GYM")
    public GymResponse updateGymStatus(UUID gymId, String status) {
        Gym gym = findGymOrThrow(gymId);
        gym.setStatus(status);
        return gymMapper.toResponse(gymRepository.save(gym));
    }

    private Gym findGymOrThrow(UUID gymId) {
        return gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym", gymId));
    }
}
