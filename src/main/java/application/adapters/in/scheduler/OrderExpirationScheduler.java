package application.adapters.in.scheduler;

import application.domain.ports.in.ReleaseExpiredOrdersUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Inbound scheduled adapter that periodically triggers {@link ReleaseExpiredOrdersUseCase}
 * to release stock reservations held by expired unpaid orders.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpirationScheduler {

    private final ReleaseExpiredOrdersUseCase releaseExpiredOrdersUseCase;

    @Scheduled(fixedDelayString = "${app.scheduler.order-expiration-delay-ms:60000}")
    public void runOrderExpirationCheck() {
        int released = releaseExpiredOrdersUseCase.releaseExpiredOrders();
        if (released > 0) {
            log.info("OrderExpirationScheduler released stock reservations for {} expired order(s).", released);
        }
    }
}
