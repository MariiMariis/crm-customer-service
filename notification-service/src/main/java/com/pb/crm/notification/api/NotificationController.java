package com.pb.crm.notification.api;

import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.web.PageResponse;
import com.pb.crm.notification.application.inbox.InboxService;
import com.pb.crm.notification.application.inbox.NotificationResponse;
import com.pb.crm.notification.domain.notification.NotificationChannel;
import com.pb.crm.notification.domain.notification.NotificationCriteria;
import com.pb.crm.notification.domain.notification.NotificationStatus;
import com.pb.crm.notification.domain.notification.NotificationType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final InboxService inboxService;

    public NotificationController(InboxService inboxService) {
        this.inboxService = inboxService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> search(
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        NotificationCriteria criteria = new NotificationCriteria(recipientId, channel, status, type, unreadOnly);
        return ResponseEntity.ok(PageResponse.from(inboxService.search(criteria, new PageQuery(page, pageSize))));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@RequestParam Long recipientId) {
        return ResponseEntity.ok(Map.of("recipientId", recipientId, "unread", inboxService.unreadCount(recipientId)));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long id,
                                                         @RequestParam(required = false) Long recipientId) {
        return ResponseEntity.ok(inboxService.markRead(id, recipientId));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllRead(@RequestParam Long recipientId) {
        return ResponseEntity.ok(Map.of("recipientId", recipientId, "marked", inboxService.markAllRead(recipientId)));
    }

    @GetMapping("/stats")
    public ResponseEntity<InboxService.Stats> stats() {
        return ResponseEntity.ok(inboxService.stats());
    }
}
