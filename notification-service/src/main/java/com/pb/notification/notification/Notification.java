package com.pb.notification.notification;

import com.pb.notification.common.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_ticket", columnList = "ticket_id"),
                @Index(name = "idx_notifications_customer", columnList = "customer_id"),
                @Index(name = "idx_notifications_status", columnList = "status"),
                @Index(name = "idx_notifications_created_at", columnList = "created_at")
        }
)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifications_seq")
    @SequenceGenerator(name = "notifications_seq", sequenceName = "notifications_seq", allocationSize = 50)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "recipient_name", nullable = false, length = 120)
    private String recipientName;

    @Column(name = "recipient_email", length = 160)
    private String recipientEmail;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(nullable = false, length = 160)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "requested_by", length = 120)
    private String requestedBy;

    @Column(nullable = false)
    private int attempts;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "read_at")
    private Instant readAt;

    protected Notification() {
    }

    public Notification(Long ticketId,
                        Long customerId,
                        String recipientName,
                        String recipientEmail,
                        String recipientPhone,
                        NotificationChannel channel,
                        NotificationType type,
                        String subject,
                        String message,
                        String requestedBy) {
        this.ticketId = ticketId;
        this.customerId = customerId;
        this.recipientName = recipientName;
        this.recipientEmail = blankToNull(recipientEmail);
        this.recipientPhone = blankToNull(recipientPhone);
        this.channel = channel;
        this.type = type;
        this.subject = subject;
        this.message = message;
        this.requestedBy = blankToNull(requestedBy) == null ? "system" : requestedBy.trim();
        this.status = NotificationStatus.PENDING;
        this.attempts = 0;
    }

    public void skip(String reason) {
        if (this.status != NotificationStatus.PENDING) {
            throw new BusinessRuleException("apenas notificacoes pendentes podem ser ignoradas");
        }
        this.status = NotificationStatus.SKIPPED;
        this.failureReason = reason;
    }

    public void assertDispatchable(int maxAttempts) {
        if (this.status == NotificationStatus.SENT) {
            throw new BusinessRuleException("a notificacao ja foi enviada");
        }
        if (this.status == NotificationStatus.SKIPPED) {
            throw new BusinessRuleException("a notificacao foi ignorada pelas preferencias do cliente e nao pode ser enviada");
        }
        if (this.status == NotificationStatus.FAILED && this.attempts >= maxAttempts) {
            throw new BusinessRuleException("limite de %d tentativas de envio atingido".formatted(maxAttempts));
        }
    }

    public void markSent() {
        this.attempts++;
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
        this.failureReason = null;
    }

    public void markFailed(String reason) {
        this.attempts++;
        this.status = NotificationStatus.FAILED;
        this.failureReason = reason;
    }

    public void markRead() {
        if (this.status != NotificationStatus.SENT) {
            throw new BusinessRuleException("apenas notificacoes enviadas podem ser marcadas como lidas");
        }
        if (this.readAt != null) {
            throw new BusinessRuleException("a notificacao ja foi marcada como lida");
        }
        this.readAt = Instant.now();
    }

    public boolean hasRecipientContact() {
        if (channel.requiresEmail()) {
            return recipientEmail != null;
        }
        if (channel.requiresPhone()) {
            return recipientPhone != null;
        }
        return true;
    }

    public String recipientAddress() {
        return switch (channel) {
            case EMAIL -> recipientEmail;
            case SMS -> recipientPhone;
            case IN_APP -> "cliente#" + customerId;
        };
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getReadAt() {
        return readAt;
    }
}
