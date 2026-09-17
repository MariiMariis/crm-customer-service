package com.pb.notification.preference;

import com.pb.notification.notification.NotificationChannel;
import com.pb.notification.preference.dto.NotificationPreferenceRequest;
import com.pb.notification.preference.dto.NotificationPreferenceResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository repository;

    public NotificationPreferenceServiceImpl(NotificationPreferenceRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferenceResponse findByCustomer(Long customerId) {
        return repository.findByCustomerId(customerId)
                .map(NotificationPreferenceResponse::fromEntity)
                .orElseGet(() -> NotificationPreferenceResponse.defaults(customerId));
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse update(Long customerId, NotificationPreferenceRequest request) {
        NotificationPreference preference = repository.findByCustomerId(customerId)
                .orElseGet(() -> NotificationPreference.defaults(customerId));
        preference.update(request.emailEnabled(), request.smsEnabled(), request.inAppEnabled());
        return NotificationPreferenceResponse.fromEntity(repository.save(preference));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isChannelEnabled(Long customerId, NotificationChannel channel) {
        return repository.findByCustomerId(customerId)
                .map(preference -> preference.isEnabled(channel))
                .orElse(true);
    }
}
