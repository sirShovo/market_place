package com.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Market Place API.
 * This application is built using Hexagonal Architecture (Ports and Adapters) 
 * combined with Domain-Driven Design (DDD) principles.
 *
 * Layers:
 * - domain/: Pure business logic, no framework dependencies.
 * - application/: Use cases, Input/Output Ports, DTOs.
 * - adapter/in/web/: REST Controllers (Driving Adapters).
 * - adapter/out/: JPA Persistence, External Clients (Driven Adapters).
 * - config/: Spring configuration, Security, Exception handling.
 * - shared/: Shared utilities and constants.
 */
@SpringBootApplication
@EnableScheduling
public class MarketPlaceApplication {

    /**
     * Application entry point.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(MarketPlaceApplication.class, args);
    }
}
