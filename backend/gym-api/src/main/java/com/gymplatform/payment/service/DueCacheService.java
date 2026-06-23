package com.gymplatform.payment.service;

import com.gymplatform.membership.domain.Membership;
import com.gymplatform.payment.domain.Due;
import com.gymplatform.payment.domain.Payment;
import com.gymplatform.payment.repository.DueRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Maintains the {@code dues} table as a read-optimization cache of {@link DueEngineService}
 * output. Never the source of truth — always rebuildable from membership + payment data.
 */
@Service
public class DueCacheService {

    private static final Set<String> CACHEABLE_STATUSES = Set.of(
            Membership.ACTIVE, Membership.DUE_SOON, Membership.OVERDUE, Membership.EXPIRED, Membership.RENEWED);

    private final DueEngineService dueEngineService;
    private final DueRepository dueRepository;

    public DueCacheService(DueEngineService dueEngineService, DueRepository dueRepository) {
        this.dueEngineService = dueEngineService;
        this.dueRepository = dueRepository;
    }

    @Transactional
    public DueComputation refresh(Membership membership, List<Payment> payments) {
        DueComputation computation = dueEngineService.compute(membership, payments, LocalDate.now());

        if (!CACHEABLE_STATUSES.contains(computation.status())) {
            return computation;
        }

        Due due = dueRepository.findByMembershipId(membership.getId())
                .orElseGet(() -> Due.builder().membershipId(membership.getId()).build());
        due.setNextDueDate(computation.nextDueDate());
        due.setPendingAmount(computation.pendingAmount());
        due.setCachedStatus(computation.status());
        due.setLastComputedAt(Instant.now());
        dueRepository.save(due);

        return computation;
    }
}
