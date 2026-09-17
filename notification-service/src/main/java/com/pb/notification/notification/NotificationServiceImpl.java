package com.pb.notification.notification;

import com.pb.notification.common.ResourceNotFoundException;
import com.pb.notification.config.NotificationProperties;
import com.pb.notification.notification.dto.NotificationRequest;
import com.pb.notification.notification.dto.NotificationResponse;
import com.pb.notification.notification.dto.NotificationStatsResponse;
import com.pb.notification.preference.NotificationPreferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository repository;
    private final NotificationPreferenceService preferenceService;
    private final NotificationSender sender;
    private final NotificationProperties properties;

    public NotificationServiceImpl(NotificationRepository repository,
                                   NotificationPreferenceService preferenceService,
                                   NotificationSender sender,
                                   NotificationProperties properties) {
        this.repository = repository;
        this.preferenceService = preferenceService;
        this.sender = sender;
        this.properties = properties;
    }

    @Override
    @Transactional
    public NotificationResponse create(NotificationRequest request) {
        Notification notification = new Notification(
                request.ticketId(),
                request.customerId(),
                request.recipientName(),
                request.recipientEmail(),
                request.recipientPhone(),
                request.channel(),
                request.type(),
                request.subject(),
                request.message(),
                request.requestedBy()
        );
        if (!preferenceService.isChannelEnabled(request.customerId(), request.channel())) {
            notification.skip("canal %s desabilitado nas preferencias do cliente".formatted(request.channel()));
        }
        return NotificationResponse.fromEntity(repository.save(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse findById(Long id) {
        return NotificationResponse.fromEntity(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> search(NotificationFilter filter) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
        return repository.findAll(NotificationSpecifications.withFilter(filter), sort).stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> findByTicket(Long ticketId) {
        return repository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponse dispatch(Long id) {
        Notification notification = findEntityById(id);
        notification.assertDispatchable(properties.maxAttempts());
        deliver(notification);
        return NotificationResponse.fromEntity(notification);
    }

    @Override
    @Transactional
    public int dispatchPending() {
        List<Notification> batch = repository.findDispatchable(
                properties.maxAttempts(),
                PageRequest.of(0, properties.dispatch().batchSize()));
        batch.forEach(this::deliver);
        return batch.size();
    }

    @Override
    @Transactional
    public NotificationResponse markRead(Long id) {
        Notification notification = findEntityById(id);
        notification.markRead();
        return NotificationResponse.fromEntity(notification);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationStatsResponse stats() {
        Map<NotificationStatus, Long> byStatus = new EnumMap<>(NotificationStatus.class);
        for (NotificationStatus status : NotificationStatus.values()) {
            byStatus.put(status, 0L);
        }
        long total = 0;
        for (NotificationStatusCount count : repository.countGroupedByStatus()) {
            byStatus.put(count.getStatus(), count.getTotal());
            total += count.getTotal();
        }
        Map<NotificationChannel, Long> byChannel = new EnumMap<>(NotificationChannel.class);
        for (NotificationChannel channel : NotificationChannel.values()) {
            byChannel.put(channel, 0L);
        }
        for (NotificationChannelCount count : repository.countGroupedByChannel()) {
            byChannel.put(count.getChannel(), count.getTotal());
        }
        long unread = repository.countByStatusAndReadAtIsNull(NotificationStatus.SENT);
        return new NotificationStatsResponse(total, unread, byStatus, byChannel);
    }

    private void deliver(Notification notification) {
        try {
            sender.send(notification);
            notification.markSent();
        } catch (RuntimeException ex) {
            log.warn("Falha ao enviar notificacao #{} pelo canal {}: {}",
                    notification.getId(), notification.getChannel(), ex.getMessage());
            notification.markFailed(ex.getMessage());
        }
    }

    private Notification findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Notificacao", id));
    }
}
