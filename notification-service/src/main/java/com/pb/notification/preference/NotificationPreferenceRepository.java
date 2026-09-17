package com.pb.notification.preference;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);

    long countByEmailEnabledFalse();
}
