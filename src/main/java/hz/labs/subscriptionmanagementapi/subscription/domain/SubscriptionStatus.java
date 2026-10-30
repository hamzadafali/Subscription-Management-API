package hz.labs.subscriptionmanagementapi.subscription.domain;

/**
 * Cycle de vie d'un abonnement.
 *
 * <pre>
 * PENDING ──activate──► ACTIVE ──suspend──► SUSPENDED
 *    │                  │  ▲  ◄───resume────    │
 *    │                  │  └ renewalSucceeded   │
 *    │                  ├──renewalFailed──► EXPIRED
 *    │                  └──terminate──┐         │
 *    └──cancel──────────────────► TERMINATED ◄──terminate
 * </pre>
 */
public enum SubscriptionStatus {
    PENDING,
    ACTIVE,
    SUSPENDED,
    TERMINATED,
    EXPIRED;

    public boolean isFinal() {
        return this == TERMINATED || this == EXPIRED;
    }
}
