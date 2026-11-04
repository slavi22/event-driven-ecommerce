package com.eventdriven.notification.application.port.out;

import com.eventdriven.notification.application.port.out.model.Notification;

public interface NotificationPort {
    void send(Notification notification);
}
