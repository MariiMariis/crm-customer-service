package com.pb.crm.notification.application.preference;

import com.pb.crm.notification.domain.preference.NotificationPreference;
import com.pb.crm.notification.domain.preference.NotificationPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferenceService {

    private final NotificationPreferenceRepository repository;

    public PreferenceService(NotificationPreferenceRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public NotificationPreference find(Long salesRepId) {
        return repository.findBySalesRep(salesRepId);
    }

    @Transactional
    public NotificationPreference update(Long salesRepId, boolean emailEnabled) {
        return repository.save(repository.findBySalesRep(salesRepId).withEmail(emailEnabled));
    }
}
