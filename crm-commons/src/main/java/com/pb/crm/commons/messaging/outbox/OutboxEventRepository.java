package com.pb.crm.commons.messaging.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = """
            select * from outbox_events
            where status = 'PENDING'
            order by created_at, id
            limit :limit
            for update skip locked
            """, nativeQuery = true)
    List<OutboxEvent> lockPendingBatch(@Param("limit") int limit);

    long countByStatus(OutboxEvent.Status status);

    @Query("select min(o.createdAt) from OutboxEvent o where o.status = com.pb.crm.commons.messaging.outbox.OutboxEvent.Status.PENDING")
    Optional<Instant> oldestPendingCreatedAt();

    List<OutboxEvent> findTop20ByOrderByCreatedAtDesc();
}
