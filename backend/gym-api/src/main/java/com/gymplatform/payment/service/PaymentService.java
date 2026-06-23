package com.gymplatform.payment.service;

import com.gymplatform.common.audit.Audited;
import com.gymplatform.common.exception.ResourceNotFoundException;
import com.gymplatform.common.tenancy.TenantContextHolder;
import com.gymplatform.membership.domain.Membership;
import com.gymplatform.membership.repository.MembershipRepository;
import com.gymplatform.notification.domain.Notification;
import com.gymplatform.notification.service.NotificationService;
import com.gymplatform.payment.domain.Payment;
import com.gymplatform.payment.dto.DueResponse;
import com.gymplatform.payment.dto.PaymentResponse;
import com.gymplatform.payment.dto.RecordPaymentRequest;
import com.gymplatform.payment.mapper.PaymentMapper;
import com.gymplatform.payment.repository.PaymentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MembershipRepository membershipRepository;
    private final DueEngineService dueEngineService;
    private final DueCacheService dueCacheService;
    private final NotificationService notificationService;
    private final PaymentMapper paymentMapper;

    public PaymentService(PaymentRepository paymentRepository,
                           MembershipRepository membershipRepository,
                           DueEngineService dueEngineService,
                           DueCacheService dueCacheService,
                           NotificationService notificationService,
                           PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.membershipRepository = membershipRepository;
        this.dueEngineService = dueEngineService;
        this.dueCacheService = dueCacheService;
        this.notificationService = notificationService;
        this.paymentMapper = paymentMapper;
    }

    @Transactional
    @Audited(action = "CREATE", entityType = "PAYMENT")
    public PaymentResponse recordPayment(RecordPaymentRequest request) {
        UUID gymId = TenantContextHolder.requireGymId();
        UUID recordedBy = TenantContextHolder.get().userId();

        Membership membership = membershipRepository.findByIdAndGymId(request.membershipId(), gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", request.membershipId()));

        Payment payment = Payment.builder()
                .membershipId(membership.getId())
                .amount(request.amount())
                .paymentType(request.paymentType())
                .paymentMethod(request.paymentMethod())
                .receiptNumber(generateReceiptNumber())
                .recordedBy(recordedBy)
                .build();
        payment = paymentRepository.save(payment);

        refreshDueCache(membership);
        notifyPaymentConfirmation(gymId, membership, payment);

        return paymentMapper.toResponse(payment);
    }

    public List<PaymentResponse> getPaymentHistory(UUID membershipId) {
        UUID gymId = TenantContextHolder.requireGymId();
        membershipRepository.findByIdAndGymId(membershipId, gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", membershipId));

        return paymentRepository.findByMembershipIdOrderByPaidAtDesc(membershipId).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    public DueResponse getDueStatus(UUID membershipId) {
        UUID gymId = TenantContextHolder.requireGymId();
        Membership membership = membershipRepository.findByIdAndGymId(membershipId, gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", membershipId));

        List<Payment> payments = paymentRepository.findByMembershipIdOrderByPaidAtDesc(membershipId);
        DueComputation computation = dueEngineService.compute(membership, payments, LocalDate.now());

        return new DueResponse(
                membershipId,
                computation.status(),
                computation.pendingAmount(),
                computation.nextDueDate(),
                computation.remainingDays(),
                computation.overdueDays(),
                computation.membershipExpiryDate()
        );
    }

    private void refreshDueCache(Membership membership) {
        List<Payment> payments = paymentRepository.findByMembershipIdOrderByPaidAtDesc(membership.getId());
        dueCacheService.refresh(membership, payments);
    }

    private void notifyPaymentConfirmation(UUID gymId, Membership membership, Payment payment) {
        notificationService.send(
                gymId,
                membership.getMemberId(),
                Notification.PAYMENT_CONFIRMATION,
                Notification.IN_APP,
                Map.of(
                        "amount", payment.getAmount(),
                        "receiptNumber", payment.getReceiptNumber(),
                        "membershipId", membership.getId().toString()
                )
        );
    }

    private String generateReceiptNumber() {
        return "RCPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
