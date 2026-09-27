package com.pb.crm.notification.domain.notification;

import com.pb.crm.commons.domain.AggregateRoot;
import com.pb.crm.commons.domain.AuditInfo;
import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.notification.domain.alert.Alert;
import com.pb.crm.notification.domain.preference.NotificationPreference;
import com.pb.crm.notification.domain.recipient.Recipient;

import java.time.Instant;
import java.util.UUID;

public class Notification extends AggregateRoot {

    public static final String DISPATCH_REQUESTED = "notification.dispatch-requested";

    private Long recipientId;
    private String recipientName;
    private String recipientEmail;
    private NotificationChannel channel;
    private NotificationType type;
    private String title;
    private String message;
    private String link;
    private UUID sourceEventId;
    private NotificationStatus status;
    private String skipReason;
    private Instant sentAt;
    private Instant readAt;

    private Notification() {
    }

    public static Notification compose(Alert alert,
                                       Recipient recipient,
                                       NotificationChannel channel,
                                       NotificationPreference preference,
                                       UUID sourceEventId) {
        Notification notification = new Notification();
        notification.recipientId = recipient.id();
        notification.recipientName = recipient.name();
        notification.recipientEmail = recipient.email();
        notification.channel = channel;
        notification.type = alert.type();
        notification.title = truncate(alert.title(), 160);
        notification.message = truncate(alert.message(), 2000);
        notification.link = alert.link();
        notification.sourceEventId = sourceEventId;
        if (channel == NotificationChannel.IN_APP) {
            notification.status = NotificationStatus.SENT;
            notification.sentAt = Instant.now();
        } else if (!preference.allows(channel)) {
            notification.status = NotificationStatus.SKIPPED;
            notification.skipReason = "canal %s desligado nas preferencias do vendedor".formatted(channel);
        } else if (!recipient.hasEmail()) {
            notification.status = NotificationStatus.SKIPPED;
            notification.skipReason = "vendedor sem e-mail cadastrado";
        } else {
            notification.status = NotificationStatus.PENDING;
            notification.recordEvent(DISPATCH_REQUESTED);
        }
        return notification;
    }

    public static Notification rehydrate(Long id,
                                         Long recipientId,
                                         String recipientName,
                                         String recipientEmail,
                                         NotificationChannel channel,
                                         NotificationType type,
                                         String title,
                                         String message,
                                         String link,
                                         UUID sourceEventId,
                                         NotificationStatus status,
                                         String skipReason,
                                         Instant sentAt,
                                         Instant readAt,
                                         AuditInfo audit) {
        Notification notification = new Notification();
        notification.rehydrateBase(id, audit, false, null);
        notification.recipientId = recipientId;
        notification.recipientName = recipientName;
        notification.recipientEmail = recipientEmail;
        notification.channel = channel;
        notification.type = type;
        notification.title = title;
        notification.message = message;
        notification.link = link;
        notification.sourceEventId = sourceEventId;
        notification.status = status;
        notification.skipReason = skipReason;
        notification.sentAt = sentAt;
        notification.readAt = readAt;
        return notification;
    }

    public boolean awaitsDispatch() {
        return status == NotificationStatus.PENDING;
    }

    public void markSent() {
        if (status != NotificationStatus.PENDING) {
            throw new BusinessRuleException("apenas notificacoes pendentes podem ser enviadas");
        }
        status = NotificationStatus.SENT;
        sentAt = Instant.now();
    }

    public void markRead() {
        if (channel != NotificationChannel.IN_APP) {
            throw new BusinessRuleException("apenas notificacoes in-app podem ser marcadas como lidas");
        }
        if (readAt == null) {
            readAt = Instant.now();
        }
    }

    public boolean isUnread() {
        return channel == NotificationChannel.IN_APP && readAt == null;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getLink() {
        return link;
    }

    public UUID getSourceEventId() {
        return sourceEventId;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public String getSkipReason() {
        return skipReason;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getReadAt() {
        return readAt;
    }
}
