package application.domain.ports.in;

/**
 * Releases physical stock reservations held by orders in {@code PENDING_PAYMENT}
 * that have exceeded the configured payment expiration window.
 */
public interface ReleaseExpiredOrdersUseCase {

    /**
     * @return the number of expired orders whose stock reservations were released.
     */
    int releaseExpiredOrders();
}
