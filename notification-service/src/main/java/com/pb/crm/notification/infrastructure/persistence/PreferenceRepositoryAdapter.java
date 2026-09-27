package com.pb.crm.notification.infrastructure.persistence;

import com.pb.crm.notification.domain.preference.NotificationPreference;
import com.pb.crm.notification.domain.preference.NotificationPreferenceRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class PreferenceRepositoryAdapter implements NotificationPreferenceRepository {

    private final SpringDataPreferenceRepository jpaRepository;

    public PreferenceRepositoryAdapter(SpringDataPreferenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public NotificationPreference findBySalesRep(Long salesRepId) {
        return jpaRepository.findById(salesRepId)
                .map(entity -> new NotificationPreference(entity.getSalesRepId(), entity.isEmailEnabled(), true,
                        entity.getUpdatedAt()))
                .orElseGet(() -> NotificationPreference.defaults(salesRepId));
    }

    @Override
    public NotificationPreference save(NotificationPreference preference) {
        PreferenceJpaEntity entity = jpaRepository.findById(preference.salesRepId())
                .orElseGet(() -> new PreferenceJpaEntity(preference.salesRepId()));
        entity.apply(preference.emailEnabled(), preference.updatedAt() == null ? Instant.now() : preference.updatedAt());
        PreferenceJpaEntity saved = jpaRepository.save(entity);
        return new NotificationPreference(saved.getSalesRepId(), saved.isEmailEnabled(), true, saved.getUpdatedAt());
    }
}
