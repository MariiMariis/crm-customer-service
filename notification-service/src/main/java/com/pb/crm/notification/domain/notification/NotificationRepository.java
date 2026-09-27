package com.pb.crm.notification.domain.notification;

import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(Long id);

    PageResult<Notification> search(NotificationCriteria criteria, PageQuery page);

    List<Notification> findUnread(Long recipientId);

    long countUnread(Long recipientId);

    Map<NotificationStatus, Long> countByStatus();

    Map<NotificationChannel, Long> countByChannel();
}
