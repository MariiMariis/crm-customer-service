package com.pb.crm.notification.infrastructure.persistence;

import com.pb.crm.notification.domain.recipient.Recipient;
import com.pb.crm.notification.domain.recipient.RecipientRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class RecipientRepositoryAdapter implements RecipientRepository {

    private final SpringDataRecipientRepository jpaRepository;

    public RecipientRepositoryAdapter(SpringDataRecipientRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Recipient> findById(Long id) {
        return jpaRepository.findById(id).map(entity -> new Recipient(entity.getId(), entity.getName(),
                entity.getEmail(), entity.getManagerId(), entity.isActive(), entity.isArchived()));
    }

    @Override
    public boolean upsert(Recipient recipient, Instant occurredAt) {
        RecipientJpaEntity entity = jpaRepository.findById(recipient.id())
                .orElseGet(() -> new RecipientJpaEntity(recipient.id()));
        if (entity.isStaleComparedTo(occurredAt)) {
            return false;
        }
        entity.apply(recipient.name(), recipient.email(), recipient.managerId(), recipient.active(),
                recipient.archived(), occurredAt);
        jpaRepository.save(entity);
        return true;
    }
}
