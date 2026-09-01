package application;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Full application-context test. Disabled until Phase 5: the domain services are wired
 * as {@code @Service} beans that depend on output ports whose adapters (persistence,
 * payment, notification) are not implemented yet, so the context cannot be built.
 * Domain behaviour is covered by plain unit tests under {@code src/test/java/application/domain}.
 */
@Disabled("Enabled in Phase 5 once output-port adapters exist")
@SpringBootTest
class NexusMarketApplicationTests {

    @Test
    void contextLoads() {
    }
}
