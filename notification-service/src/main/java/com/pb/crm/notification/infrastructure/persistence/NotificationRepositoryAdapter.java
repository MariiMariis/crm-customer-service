package com.pb.crm.notification.infrastructure.persistence;

import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.ResourceNotFoundException;
import com.pb.crm.notification.domain.notification.Notification;
import com.pb.crm.notification.domain.notification.NotificationChannel;
import com.pb.crm.notification.domain.notification.NotificationCriteria;
import com.pb.crm.notification.domain.notification.NotificationRepository;
import com.pb.crm.notification.domain.notification.NotificationStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final SpringDataNotificationRepository jpaRepository;

    public NotificationRepositoryAdapter(SpringDataNotificationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = notification.isNew()
                ? new NotificationJpaEntity()
                : jpaRepository.findById(notification.getId())
                        .orElseThrow(() -> ResourceNotFoundException.forId("Notificacao", notification.getId()));
        entity.apply(notification.getRecipientId(), notification.getRecipientName(), notification.getRecipientEmail(),
                notification.getChannel(), notification.getType(), notification.getTitle(), notification.getMessage(),
                notification.getLink(), notification.getSourceEventId(), notification.getStatus(),
                notification.getSkipReason(), notification.getSentAt(), notification.getReadAt());
        return toDomain(jpaRepository.saveAndFlush(entity));
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return jpaRepository.findById(id).map(NotificationRepositoryAdapter::toDomain);
    }

    @Override
    public PageResult<Notification> search(NotificationCriteria criteria, PageQuery page) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
        Page<NotificationJpaEntity> result = jpaRepository.findAll(matching(criteria), PageRequest.of(page.page(), page.size(), sort));
        return new PageResult<>(
                result.getContent().stream().map(NotificationRepositoryAdapter::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
    }

    @Override
    public List<Notification> findUnread(Long recipientId) {
        return jpaRepository.findByRecipientIdAndChannelAndReadAtIsNullOrderByCreatedAtDesc(recipientId, NotificationChannel.IN_APP)
                .stream()
                .map(NotificationRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public long countUnread(Long recipientId) {
        return jpaRepository.countByRecipientIdAndChannelAndReadAtIsNull(recipientId, NotificationChannel.IN_APP);
    }

    @Override
    public Map<NotificationStatus, Long> countByStatus() {
        Map<NotificationStatus, Long> counts = new EnumMap<>(NotificationStatus.class);
        for (NotificationStatus status : NotificationStatus.values()) {
            counts.put(status, 0L);
        }
        jpaRepository.countGroupedByStatus().forEach(row -> counts.put((NotificationStatus) row[0], (Long) row[1]));
        return counts;
    }

    @Override
    public Map<NotificationChannel, Long> countByChannel() {
        Map<NotificationChannel, Long> counts = new EnumMap<>(NotificationChannel.class);
        for (NotificationChannel channel : NotificationChannel.values()) {
            counts.put(channel, 0L);
        }
        jpaRepository.countGroupedByChannel().forEach(row -> counts.put((NotificationChannel) row[0], (Long) row[1]));
        return counts;
    }

    private static Specification<NotificationJpaEntity> matching(NotificationCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.recipientId() != null) {
                predicates.add(cb.equal(root.get("recipientId"), criteria.recipientId()));
            }
            if (criteria.channel() != null) {
                predicates.add(cb.equal(root.get("channel"), criteria.channel()));
            }
            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }
            if (criteria.type() != null) {
                predicates.add(cb.equal(root.get("type"), criteria.type()));
            }
            if (criteria.unreadOnly()) {
                predicates.add(cb.equal(root.get("channel"), NotificationChannel.IN_APP));
                predicates.add(cb.isNull(root.get("readAt")));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static Notification toDomain(NotificationJpaEntity entity) {
        return Notification.rehydrate(entity.getId(), entity.getRecipientId(), entity.getRecipientName(),
                entity.getRecipientEmail(), entity.getChannel(), entity.getType(), entity.getTitle(), entity.getMessage(),
                entity.getLink(), entity.getSourceEventId(), entity.getStatus(), entity.getSkipReason(),
                entity.getSentAt(), entity.getReadAt(), entity.toAuditInfo());
    }
}
