package application.domain.ports.out;

import application.domain.models.Notification;

/** Abstracts communication with external notification systems (e-mail / SMS / push). */
public interface NotificationPort {

    void send(Notification notification);
}
