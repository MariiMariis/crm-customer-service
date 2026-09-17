package com.pb.notification.notification;

public enum NotificationChannel {
    EMAIL,
    SMS,
    IN_APP;

    public boolean requiresEmail() {
        return this == EMAIL;
    }

    public boolean requiresPhone() {
        return this == SMS;
    }
}
