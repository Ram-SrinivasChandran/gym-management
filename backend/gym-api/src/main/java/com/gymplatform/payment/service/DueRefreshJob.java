package com.gymplatform.payment.service;

import com.gymplatform.membership.domain.Membership;
import com.gymplatform.membership.repository.MembershipRepository;
import com.gymplatform.payment.domain.Payment;
import com.gymplatform.payment.repository.PaymentRepository;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Nightly refresh of the {@code dues} cache for every non-terminal membership, so list/filter
 * views (due-today, overdue, etc.) stay fast without recomputing the due engine on every read.
 * The cache is a convenience — see {@link DueCacheService}.
 */
@Component
public class DueRefreshJob {

    private final MembershipRepository membershipRepository;
    private final PaymentRepository paymentRepository;
    private final DueCacheService dueCacheService;

    public DueRefreshJob(MembershipRepository membershipRepository,
                          PaymentRepository paymentRepository,
                          DueCacheService dueCacheService) {
        this.membershipRepository = membershipRepository;
        this.paymentRepository = paymentRepository;
        this.dueCacheService = dueCacheService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void refreshAll() {
        for (Membership membership : membershipRepository.findAllNonTerminal()) {
            List<Payment> payments = paymentRepository.findByMembershipIdOrderByPaidAtDesc(membership.getId());
            dueCacheService.refresh(membership, payments);
        }
    }
}
