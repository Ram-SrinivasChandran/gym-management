package com.gymplatform.payment.api;

import com.gymplatform.payment.dto.DueResponse;
import com.gymplatform.payment.dto.PaymentResponse;
import com.gymplatform.payment.dto.RecordPaymentRequest;
import com.gymplatform.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments")
@PreAuthorize("hasAnyRole('GYM_ADMIN','TRAINER')")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse recordPayment(@Valid @RequestBody RecordPaymentRequest request) {
        return paymentService.recordPayment(request);
    }

    @GetMapping("/membership/{membershipId}/history")
    public List<PaymentResponse> getHistory(@PathVariable UUID membershipId) {
        return paymentService.getPaymentHistory(membershipId);
    }

    @GetMapping("/membership/{membershipId}/due")
    public DueResponse getDue(@PathVariable UUID membershipId) {
        return paymentService.getDueStatus(membershipId);
    }
}
