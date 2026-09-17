package com.pb.notification.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>,
        JpaSpecificationExecutor<Notification> {

    List<Notification> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

    List<Notification> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    long countByStatus(NotificationStatus status);

    long countByTicketId(Long ticketId);

    long countByStatusAndReadAtIsNull(NotificationStatus status);

    @Query("""
            select n from Notification n
            where n.status = com.pb.notification.notification.NotificationStatus.PENDING
               or (n.status = com.pb.notification.notification.NotificationStatus.FAILED and n.attempts < :maxAttempts)
            order by n.createdAt asc, n.id asc
            """)
    List<Notification> findDispatchable(@Param("maxAttempts") int maxAttempts, Pageable pageable);

    @Query("select n.status as status, count(n) as total from Notification n group by n.status")
    List<NotificationStatusCount> countGroupedByStatus();

    @Query("select n.channel as channel, count(n) as total from Notification n group by n.channel")
    List<NotificationChannelCount> countGroupedByChannel();
}
