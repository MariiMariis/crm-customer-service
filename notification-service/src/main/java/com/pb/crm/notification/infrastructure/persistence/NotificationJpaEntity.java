package com.pb.crm.notification.infrastructure.persistence;

import com.pb.crm.commons.audit.AuditableEntity;
import com.pb.crm.notification.domain.notification.NotificationChannel;
import com.pb.crm.notification.domain.notification.NotificationStatus;
import com.pb.crm.notification.domain.notification.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationJpaEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifications_seq")
    @SequenceGenerator(name = "notifications_seq", sequenceName = "notifications_seq", allocationSize = 50)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "recipient_name", nullable = false, length = 120)
    private String recipientName;

    @Column(name = "recipient_email", length = 160)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(length = 200)
    private String link;

    @Column(name = "source_event_id", nullable = false)
    private UUID sourceEventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(name = "skip_reason", length = 300)
    private String skipReason;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "read_at")
    private Instant readAt;

    public void apply(Long recipientId,
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
                      Instant readAt) {
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.recipientEmail = recipientEmail;
        this.channel = channel;
        this.type = type;
        this.title = title;
        this.message = message;
        this.link = link;
        this.sourceEventId = sourceEventId;
        this.status = status;
        this.skipReason = skipReason;
        this.sentAt = sentAt;
        this.readAt = readAt;
    }

    public Long getId() {
        return id;
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
