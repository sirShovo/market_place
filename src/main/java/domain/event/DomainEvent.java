package domain.event;

import java.time.LocalDateTime;

/**
 * Interface representing a generic Domain Event.
 * All domain events must implement this interface to ensure
 * they provide an occurrence timestamp.
 */
public interface DomainEvent {
    
    /**
     * Gets the timestamp when the event occurred.
     *
     * @return LocalDateTime of the event occurrence.
     */
    LocalDateTime getOccurredOn();
}
