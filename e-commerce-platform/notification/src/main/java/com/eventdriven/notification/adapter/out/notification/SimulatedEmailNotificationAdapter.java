package com.eventdriven.notification.adapter.out.notification;

import com.eventdriven.notification.application.port.out.NotificationPort;
import com.eventdriven.notification.application.port.out.model.Notification;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
class SimulatedEmailNotificationAdapter implements NotificationPort {

    @Override
    public void send(Notification notification) {
        log.info("[EMAIL] To: {} | Subject: {} | Body: {}",
                notification.recipientId(), notification.subject(), notification.body());
    }
}
