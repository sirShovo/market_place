package com.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MARKET PLACE API
 * Architecture: Hexagonal (Ports and Adapters) + DDD
 *
 * Layers:
 *   domain/          -> Lógica de negocio pura, sin dependencias de framework.
 *   application/     -> Casos de uso, Puertos de Entrada/Salida, DTOs.
 *   adapter/in/web/  -> Controladores REST (Driving Adapters).
 *   adapter/out/     -> Persistencia JPA, Clientes Externos (Driven Adapters).
 *   config/          -> Configuración de Spring, Seguridad, Manejo de Excepciones.
 *   shared/          -> Utilidades y constantes compartidas.
 */
@SpringBootApplication
@EnableScheduling
public class MarketPlaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketPlaceApplication.class, args);
    }
}
