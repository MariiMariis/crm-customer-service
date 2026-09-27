package com.pb.crm.notification.application.inbox;

import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.commons.error.ResourceNotFoundException;
import com.pb.crm.notification.domain.notification.Notification;
import com.pb.crm.notification.domain.notification.NotificationChannel;
import com.pb.crm.notification.domain.notification.NotificationCriteria;
import com.pb.crm.notification.domain.notification.NotificationRepository;
import com.pb.crm.notification.domain.notification.NotificationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class InboxService {

    public record Stats(long total, Map<NotificationStatus, Long> byStatus, Map<NotificationChannel, Long> byChannel) {
    }

    private final NotificationRepository repository;

    public InboxService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PageResult<NotificationResponse> search(NotificationCriteria criteria, PageQuery page) {
        return repository.search(criteria, page).map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long recipientId) {
        return repository.countUnread(recipientId);
    }

    @Transactional
    public NotificationResponse markRead(Long id, Long recipientId) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Notificacao", id));
        if (recipientId != null && !recipientId.equals(notification.getRecipientId())) {
            throw new BusinessRuleException("a notificacao pertence a outro vendedor");
        }
        notification.markRead();
        return NotificationResponse.from(repository.save(notification));
    }

    @Transactional
    public int markAllRead(Long recipientId) {
        List<Notification> unread = repository.findUnread(recipientId);
        unread.forEach(notification -> {
            notification.markRead();
            repository.save(notification);
        });
        return unread.size();
    }

    @Transactional(readOnly = true)
    public Stats stats() {
        Map<NotificationStatus, Long> byStatus = repository.countByStatus();
        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        return new Stats(total, byStatus, repository.countByChannel());
    }
}
