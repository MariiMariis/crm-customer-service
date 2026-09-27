package com.pb.crm.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPreferenceRepository extends JpaRepository<PreferenceJpaEntity, Long> {
}
