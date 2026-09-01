package application.port.output;

import application.domain.event.DomainEvent;

/**
 * Output Port for sending notifications via external services (e.g., Email).
 */
public interface EmailNotificationPort {

    /**
     * Dispatches an email notification based on a domain event.
     *
     * @param event The domain event triggering the notification.
     * @param recipientEmail The destination email address.
     */
    void sendNotification(DomainEvent event, String recipientEmail);
}
