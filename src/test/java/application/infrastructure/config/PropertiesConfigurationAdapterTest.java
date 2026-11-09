package application.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PropertiesConfigurationAdapterTest {

    @Test
    void returnsConfiguredUnpaidOrderExpirationMinutes() {
        PropertiesConfigurationAdapter adapter = new PropertiesConfigurationAdapter(45);
        assertEquals(45, adapter.getUnpaidOrderExpirationMinutes());
    }
}
