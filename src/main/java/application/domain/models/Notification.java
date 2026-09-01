package application.domain.models;

import application.domain.enums.NotificationChannel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A message the domain asks to deliver to a participant through {@code NotificationPort}.
 * Keeps transport-specific concerns (addresses, formatting) out of the domain.
 */
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    private NotificationChannel channel;
    private String recipient;
    private String subject;
    private String body;
}
