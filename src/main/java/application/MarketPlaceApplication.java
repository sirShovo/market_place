package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main entry point for the Market Place API.
 * Uses component scanning to load all hexagonal layers.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"application", "domain", "adapter", "config", "shared"})
public class MarketPlaceApplication {

    /**
     * Main method to start the Spring Boot application.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(MarketPlaceApplication.class, args);
    }
}
