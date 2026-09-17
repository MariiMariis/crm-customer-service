package com.pb.notification.notification;

import com.pb.notification.notification.dto.NotificationRequest;
import com.pb.notification.notification.dto.NotificationResponse;
import com.pb.notification.notification.dto.NotificationStatsResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse created = notificationService.create(request);
        return ResponseEntity.created(URI.create("/api/notifications/" + created.id())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> search(
            @RequestParam(required = false) Long ticketId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) NotificationType type) {
        NotificationFilter filter = new NotificationFilter(ticketId, customerId, status, channel, type);
        return ResponseEntity.ok(notificationService.search(filter));
    }

    @GetMapping("/stats")
    public ResponseEntity<NotificationStatsResponse> stats() {
        return ResponseEntity.ok(notificationService.stats());
    }

    @PostMapping("/dispatch")
    public ResponseEntity<DispatchResult> dispatchPending() {
        return ResponseEntity.ok(new DispatchResult(notificationService.dispatchPending()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.findById(id));
    }

    @PostMapping("/{id}/dispatch")
    public ResponseEntity<NotificationResponse> dispatch(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.dispatch(id));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markRead(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    public record DispatchResult(int processed) {
    }
}
