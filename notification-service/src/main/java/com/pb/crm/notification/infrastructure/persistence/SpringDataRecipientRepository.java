package com.pb.crm.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRecipientRepository extends JpaRepository<RecipientJpaEntity, Long> {
}
