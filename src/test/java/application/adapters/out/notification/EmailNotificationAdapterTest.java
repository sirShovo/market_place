package application.adapters.out.notification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import application.domain.enums.NotificationChannel;
import application.domain.models.Notification;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class EmailNotificationAdapterTest {

    @Test
    void sendsSimpleMailMessageWithNotificationFields() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailNotificationAdapter adapter = new EmailNotificationAdapter(mailSender);

        Notification notification = new Notification();
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setRecipient("buyer@test.com");
        notification.setSubject("Payment confirmed");
        notification.setBody("Your order ORD-1 has been paid.");

        adapter.send(notification);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertEquals("buyer@test.com", sent.getTo()[0]);
        assertEquals("Payment confirmed", sent.getSubject());
        assertEquals("Your order ORD-1 has been paid.", sent.getText());
    }

    @Test
    void degradesGracefullyWhenMailSenderThrowsMailException() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));
        EmailNotificationAdapter adapter = new EmailNotificationAdapter(mailSender);

        Notification notification = new Notification();
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setRecipient("buyer@test.com");
        notification.setSubject("Subj");
        notification.setBody("Body");

        assertDoesNotThrow(() -> adapter.send(notification));
    }
}
