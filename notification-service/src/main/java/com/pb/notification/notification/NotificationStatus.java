package com.pb.notification.notification;

public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    SKIPPED;

    public boolean isFinal() {
        return this == SENT || this == SKIPPED;
    }
}
