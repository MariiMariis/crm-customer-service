package com.pb.crm.notification.application.dispatch;

import com.pb.crm.notification.domain.notification.Notification;

public interface EmailSender {

    void send(Notification notification);
}
