package com.pb.crm.notification.domain.preference;

public interface NotificationPreferenceRepository {

    NotificationPreference findBySalesRep(Long salesRepId);

    NotificationPreference save(NotificationPreference preference);
}
