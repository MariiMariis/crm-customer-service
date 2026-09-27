package com.pb.crm.notification.domain.recipient;

import java.time.Instant;
import java.util.Optional;

public interface RecipientRepository {

    Optional<Recipient> findById(Long id);

    boolean upsert(Recipient recipient, Instant occurredAt);

    default void upsert(Recipient recipient) {
        upsert(recipient, Instant.now());
    }
}
