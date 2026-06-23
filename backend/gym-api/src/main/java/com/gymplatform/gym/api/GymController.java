package com.gymplatform.gym.api;

import com.gymplatform.gym.dto.GymResponse;
import com.gymplatform.gym.dto.OnboardGymRequest;
import com.gymplatform.gym.service.GymService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gyms")
@Tag(name = "Gyms")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class GymController {

    private final GymService gymService;

    public GymController(GymService gymService) {
        this.gymService = gymService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GymResponse onboardGym(@Valid @RequestBody OnboardGymRequest request) {
        return gymService.onboardGym(request);
    }

    @GetMapping
    public List<GymResponse> listGyms() {
        return gymService.listGyms();
    }

    @GetMapping("/{gymId}")
    public GymResponse getGym(@PathVariable UUID gymId) {
        return gymService.getGym(gymId);
    }

    @PatchMapping("/{gymId}/status")
    public GymResponse updateStatus(@PathVariable UUID gymId, @RequestParam String status) {
        return gymService.updateGymStatus(gymId, status);
    }
}
