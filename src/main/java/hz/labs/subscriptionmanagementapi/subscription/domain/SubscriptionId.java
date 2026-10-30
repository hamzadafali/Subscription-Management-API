package hz.labs.subscriptionmanagementapi.subscription.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifiant unique d'un abonnement.
 */
public record SubscriptionId(UUID value) {

    public SubscriptionId {
        Objects.requireNonNull(value, "value");
    }

    public static SubscriptionId newId() {
        return new SubscriptionId(UUID.randomUUID());
    }
}
