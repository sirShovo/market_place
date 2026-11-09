package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the NexusMarket API.
 * Component scanning starts at the {@code application} root package, covering the
 * hexagonal layers ({@code application.domain}, {@code application.adapters},
 * {@code application.infrastructure}).
 */
@SpringBootApplication
@EnableScheduling
public class NexusMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusMarketApplication.class, args);
    }
}
