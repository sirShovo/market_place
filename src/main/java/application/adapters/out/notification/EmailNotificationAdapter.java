package application.adapters.out.notification;

import application.domain.models.Notification;
import application.domain.ports.out.NotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Email notification adapter implementing {@link NotificationPort} via {@link JavaMailSender}.
 * Degrades gracefully when an SMTP server is unreachable in local or test environments.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationAdapter implements NotificationPort {

    private final JavaMailSender mailSender;

    @Override
    public void send(Notification notification) {
        if (notification == null || notification.getRecipient() == null) {
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notification.getRecipient());
        message.setSubject(notification.getSubject() != null ? notification.getSubject() : "");
        message.setText(notification.getBody() != null ? notification.getBody() : "");

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.warn("Could not deliver email notification to {}: {}",
                    notification.getRecipient(), ex.getMessage());
        }
    }
}
