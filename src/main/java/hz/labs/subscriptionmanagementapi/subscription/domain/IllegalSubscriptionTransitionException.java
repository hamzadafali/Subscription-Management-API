package hz.labs.subscriptionmanagementapi.subscription.domain;

/**
 * Levée lorsqu'une intention métier n'est pas autorisée dans l'état courant de l'abonnement.
 */
public class IllegalSubscriptionTransitionException extends RuntimeException {

    public IllegalSubscriptionTransitionException(String message) {
        super(message);
    }

    static IllegalSubscriptionTransitionException of(String action, SubscriptionStatus current) {
        return new IllegalSubscriptionTransitionException(
                "Cannot " + action + " a subscription in status " + current);
    }
}
