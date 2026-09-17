package com.pb.notification.preference;

import com.pb.notification.notification.NotificationChannel;
import com.pb.notification.preference.dto.NotificationPreferenceRequest;
import com.pb.notification.preference.dto.NotificationPreferenceResponse;

public interface NotificationPreferenceService {

    NotificationPreferenceResponse findByCustomer(Long customerId);

    NotificationPreferenceResponse update(Long customerId, NotificationPreferenceRequest request);

    boolean isChannelEnabled(Long customerId, NotificationChannel channel);
}
