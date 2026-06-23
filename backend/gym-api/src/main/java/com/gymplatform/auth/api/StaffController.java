package com.gymplatform.auth.api;

import com.gymplatform.auth.dto.CreateStaffRequest;
import com.gymplatform.auth.dto.StaffResponse;
import com.gymplatform.auth.service.StaffService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff")
@Tag(name = "Staff")
@PreAuthorize("hasRole('GYM_ADMIN')")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StaffResponse createStaff(@Valid @RequestBody CreateStaffRequest request) {
        return staffService.createStaff(request);
    }

    @GetMapping
    public List<StaffResponse> listStaff() {
        return staffService.listStaff();
    }

    @PatchMapping("/{staffId}/status")
    public StaffResponse updateStatus(@PathVariable UUID staffId, @RequestParam String status) {
        return staffService.updateStatus(staffId, status);
    }
}
