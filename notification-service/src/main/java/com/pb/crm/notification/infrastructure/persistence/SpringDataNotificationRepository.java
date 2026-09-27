package com.pb.crm.notification.infrastructure.persistence;

import com.pb.crm.notification.domain.notification.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, Long>,
        JpaSpecificationExecutor<NotificationJpaEntity> {

    List<NotificationJpaEntity> findByRecipientIdAndChannelAndReadAtIsNullOrderByCreatedAtDesc(Long recipientId,
                                                                                               NotificationChannel channel);

    long countByRecipientIdAndChannelAndReadAtIsNull(Long recipientId, NotificationChannel channel);

    @Query("select n.status, count(n) from NotificationJpaEntity n group by n.status")
    List<Object[]> countGroupedByStatus();

    @Query("select n.channel, count(n) from NotificationJpaEntity n group by n.channel")
    List<Object[]> countGroupedByChannel();
}
