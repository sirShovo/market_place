package application.domain.enums;

/**
 * Outcome of a payment attempt, returned by {@code PaymentGatewayPort}.
 */
public enum PaymentResult {
    APPROVED,
    REJECTED
}
