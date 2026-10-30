package hz.labs.subscriptionmanagementapi.subscription.domain;

import java.time.LocalDate;
import java.util.*;

import static hz.labs.subscriptionmanagementapi.subscription.domain.SubscriptionStatus.ACTIVE;
import static hz.labs.subscriptionmanagementapi.subscription.domain.SubscriptionStatus.EXPIRED;
import static hz.labs.subscriptionmanagementapi.subscription.domain.SubscriptionStatus.PENDING;
import static hz.labs.subscriptionmanagementapi.subscription.domain.SubscriptionStatus.SUSPENDED;
import static hz.labs.subscriptionmanagementapi.subscription.domain.SubscriptionStatus.TERMINATED;

/**
 * Abonnement d'une entreprise à un plan. Chaque méthode publique correspond à une intention
 * métier et garantit que la transition de statut est autorisée.
 */
public final class Subscription {

    private final SubscriptionId id;
    private final UUID companyId;
    private final UUID planId;
    private SubscriptionStatus status;
    private LocalDate scheduledTerminationDate;

    private Subscription(SubscriptionId id, UUID companyId, UUID planId) {
        this.id = Objects.requireNonNull(id, "id");
        this.companyId = Objects.requireNonNull(companyId, "companyId");
        this.planId = Objects.requireNonNull(planId, "planId");
        this.status = PENDING;
    }

    public static Subscription create(UUID companyId, UUID planId) {
        return new Subscription(SubscriptionId.newId(), companyId, planId);
    }

    public void activate() {
        if (status != PENDING) {
            throw IllegalSubscriptionTransitionException.of("activate", status);
        }
        status = ACTIVE;
    }

    public void cancel() {
        if (status != PENDING) {
            throw IllegalSubscriptionTransitionException.of("cancel", status);
        }
        status = TERMINATED;
    }

    public void suspend() {
        if (status != ACTIVE) {
            throw IllegalSubscriptionTransitionException.of("suspend", status);
        }
        status = SUSPENDED;
    }

    public void resume() {
        if (status != SUSPENDED) {
            throw IllegalSubscriptionTransitionException.of("resume", status);
        }
        status = ACTIVE;
    }

    public void renewalFailed() {
        if (status != ACTIVE) {
            throw IllegalSubscriptionTransitionException.of("renewalFailed", status);
        }
        status = EXPIRED;
    }

    public void renewalSucceeded() {
        requireStatus(ACTIVE, "renewalSucceeded");
    }

    private void requireStatus(SubscriptionStatus expectedStatus, String action) {
        if (status != expectedStatus) {
            throw IllegalSubscriptionTransitionException.of(action, status);
        }
        status = ACTIVE;
    }

    public void scheduleTermination(LocalDate terminationDate) {
        Objects.requireNonNull(terminationDate, "terminationDate");
        if (status != ACTIVE && status != SUSPENDED) {
            throw IllegalSubscriptionTransitionException.of("scheduleTermination", status);
        }
        scheduledTerminationDate = terminationDate;
    }

    public void terminate(LocalDate today) {
        Objects.requireNonNull(today, "today");
        if (status != ACTIVE && status != SUSPENDED) {
            throw IllegalSubscriptionTransitionException.of("terminate", status);
        }
        if (scheduledTerminationDate == null || today.isBefore(scheduledTerminationDate)) {
            throw new IllegalSubscriptionTransitionException(
                    "Cannot terminate a subscription before its scheduled termination date");
        }
        status = TERMINATED;
    }


    @Override
    public boolean equals(Object o) {
        return o instanceof Subscription other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
