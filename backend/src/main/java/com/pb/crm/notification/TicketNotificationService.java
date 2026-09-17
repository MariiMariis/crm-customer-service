package com.pb.crm.notification;

import com.pb.crm.notification.dto.ManualNotificationRequest;
import com.pb.crm.notification.dto.NotificationPreferenceRequest;
import com.pb.crm.notification.dto.NotificationPreferenceResponse;
import com.pb.crm.notification.dto.NotificationResponse;
import com.pb.crm.notification.dto.NotificationServiceStatus;

import java.util.List;

public interface TicketNotificationService {

    List<NotificationResponse> findByTicket(Long ticketId);

    NotificationResponse sendManual(Long ticketId, ManualNotificationRequest request);

    NotificationPreferenceResponse findPreferences(Long customerId);

    NotificationPreferenceResponse updatePreferences(Long customerId, NotificationPreferenceRequest request);

    NotificationServiceStatus status();
}
