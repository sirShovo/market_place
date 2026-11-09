package application.infrastructure.config;

import application.domain.ports.out.BusinessConfigurationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Exposes externalized business configuration parameters from Spring properties.
 */
@Component
public class PropertiesConfigurationAdapter implements BusinessConfigurationPort {

    private final int unpaidOrderExpirationMinutes;

    public PropertiesConfigurationAdapter(
            @Value("${app.order.unpaid-expiration-minutes:30}") int unpaidOrderExpirationMinutes) {
        this.unpaidOrderExpirationMinutes = unpaidOrderExpirationMinutes;
    }

    @Override
    public int getUnpaidOrderExpirationMinutes() {
        return unpaidOrderExpirationMinutes;
    }
}
