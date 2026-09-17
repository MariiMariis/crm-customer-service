package com.pb.crm.notification;

import com.pb.crm.notification.dto.NotificationPreferenceRequest;
import com.pb.crm.notification.dto.NotificationPreferenceResponse;
import com.pb.crm.notification.dto.NotificationRequest;
import com.pb.crm.notification.dto.NotificationResponse;
import com.pb.crm.notification.dto.NotificationServiceStatus;

import java.util.List;
import java.util.Optional;

public interface NotificationGateway {

    String CIRCUIT_BREAKER_ID = "notification-service";

    Optional<NotificationResponse> send(NotificationRequest request);

    List<NotificationResponse> findByTicket(Long ticketId);

    NotificationPreferenceResponse findPreferences(Long customerId);

    NotificationPreferenceResponse updatePreferences(Long customerId, NotificationPreferenceRequest request);

    NotificationServiceStatus status();
}
