package com.pb.notification.preference;

import com.pb.notification.notification.NotificationChannel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "notification_preferences",
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_preferences_customer", columnNames = "customer_id")
)
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_preferences_seq")
    @SequenceGenerator(name = "notification_preferences_seq", sequenceName = "notification_preferences_seq", allocationSize = 50)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Column(name = "sms_enabled", nullable = false)
    private boolean smsEnabled;

    @Column(name = "in_app_enabled", nullable = false)
    private boolean inAppEnabled;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected NotificationPreference() {
    }

    public NotificationPreference(Long customerId, boolean emailEnabled, boolean smsEnabled, boolean inAppEnabled) {
        this.customerId = customerId;
        this.emailEnabled = emailEnabled;
        this.smsEnabled = smsEnabled;
        this.inAppEnabled = inAppEnabled;
    }

    public static NotificationPreference defaults(Long customerId) {
        return new NotificationPreference(customerId, true, true, true);
    }

    public void update(boolean emailEnabled, boolean smsEnabled, boolean inAppEnabled) {
        this.emailEnabled = emailEnabled;
        this.smsEnabled = smsEnabled;
        this.inAppEnabled = inAppEnabled;
        this.updatedAt = Instant.now();
    }

    public boolean isEnabled(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> emailEnabled;
            case SMS -> smsEnabled;
            case IN_APP -> inAppEnabled;
        };
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public boolean isInAppEnabled() {
        return inAppEnabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
