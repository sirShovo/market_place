package com.marketplace.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson JSON Mapper configuration.
 *
 * Configures the global ObjectMapper used for JSON serialization and deserialization.
 * Specifically ensures that Java 8 Date and Time API classes (JSR-310) are handled
 * correctly, formatting them as ISO-8601 strings rather than arrays of timestamps.
 */
@Configuration
public class JacksonConfig {

    /**
     * Creates and customizes the primary ObjectMapper bean.
     *
     * @return The configured ObjectMapper instance.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // Disables writing dates as arrays of timestamps, preferring ISO-8601 string format instead.
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
