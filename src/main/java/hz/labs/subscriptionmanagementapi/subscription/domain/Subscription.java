package hz.labs.subscriptionmanagementapi.subscription.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
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
    private LocalDate renewalDate;
    private LocalDate activationDate;
    private LocalDate suspendedAt;

    private Subscription(SubscriptionId id, UUID companyId, UUID planId) {
        this.id = Objects.requireNonNull(id, "id");
        this.companyId = Objects.requireNonNull(companyId, "companyId");
        this.planId = Objects.requireNonNull(planId, "planId");
        this.status = PENDING;
    }

    public static Subscription create(UUID companyId, UUID planId) {
        return new Subscription(SubscriptionId.newId(), companyId, planId);
    }

    public void activate(LocalDate today) {
        Objects.requireNonNull(today, "today");
        if (status != PENDING) {
            throw IllegalSubscriptionTransitionException.of("activate", status);
        }
        activationDate = today;
        renewalDate = activationDate.plusMonths(1);
        status = ACTIVE;
    }

    public void renew(LocalDate today, boolean paymentSucceeded) {
        Objects.requireNonNull(today, "today");
        if (status != ACTIVE) {
            throw IllegalSubscriptionTransitionException.of("renew", status);
        }
        if (today.isBefore(renewalDate)) {
            throw new IllegalSubscriptionTransitionException(
                    "Cannot renew a subscription before its renewal date");
        }
        if (scheduledTerminationDate != null && !scheduledTerminationDate.isAfter(renewalDate)) {
            throw new IllegalSubscriptionTransitionException(
                    "Cannot renew a subscription terminating on or before its renewal date");
        }
        if (!paymentSucceeded) {
            status = EXPIRED;
            return;
        }
        renewalDate = renewalDate.plusMonths(1);
    }

    public void cancel() {
        if (status != PENDING) {
            throw IllegalSubscriptionTransitionException.of("cancel", status);
        }
        status = TERMINATED;
    }

    public void suspend(LocalDate today) {
        Objects.requireNonNull(today, "today");

        if (status != ACTIVE) {
            throw IllegalSubscriptionTransitionException.of(
                    "suspend", status);
        }

        suspendedAt = today;
        status = SUSPENDED;
    }

    public void resume(LocalDate today) {
        Objects.requireNonNull(today, "today");

        if (status != SUSPENDED) {
            throw IllegalSubscriptionTransitionException.of(
                    "resume", status);
        }

        if (suspendedAt == null) {
            throw new IllegalSubscriptionTransitionException(
                    "No suspension start date");
        }

        if (scheduledTerminationDate != null
                && !today.isBefore(scheduledTerminationDate)) {
            throw new IllegalSubscriptionTransitionException(
                    "Cannot resume after scheduled termination date");
        }

        if (today.isBefore(suspendedAt)) {
            throw new IllegalSubscriptionTransitionException(
                    "Resume date cannot be before suspension date");
        }

        long days = ChronoUnit.DAYS.between(suspendedAt, today);

        renewalDate = renewalDate.plusDays(days);
        suspendedAt = null;
        status = ACTIVE;
    }

    public void scheduleTermination(LocalDate today, LocalDate terminationDate) {
        Objects.requireNonNull(today, "today");
        Objects.requireNonNull(terminationDate, "terminationDate");
        if (status != ACTIVE && status != SUSPENDED) {
            throw IllegalSubscriptionTransitionException.of("scheduleTermination", status);
        }
        if (terminationDate.equals(scheduledTerminationDate)) {
            return;
        }
        if (scheduledTerminationDate != null) {
            throw new IllegalSubscriptionTransitionException(
                    "A termination is already scheduled on " + scheduledTerminationDate);
        }
        if (!terminationDate.isAfter(today)) {
            throw new IllegalSubscriptionTransitionException(
                    "Termination date must be in the future");
        }
        scheduledTerminationDate = terminationDate;
    }

    public void terminate(LocalDate today) {
        Objects.requireNonNull(today, "today");
        if (status != ACTIVE && status != SUSPENDED) {
            throw IllegalSubscriptionTransitionException.of("terminate", status);
        }
        if (scheduledTerminationDate == null) {
            throw new IllegalSubscriptionTransitionException(
                    "Cannot terminate a subscription without a scheduled termination");
        }
        if (today.isBefore(scheduledTerminationDate)) {
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
