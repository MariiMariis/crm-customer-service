package com.pb.crm.notification;

import com.pb.crm.notification.dto.ManualNotificationRequest;
import com.pb.crm.notification.dto.NotificationPreferenceRequest;
import com.pb.crm.notification.dto.NotificationPreferenceResponse;
import com.pb.crm.notification.dto.NotificationResponse;
import com.pb.crm.notification.dto.NotificationServiceStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final TicketNotificationService notificationService;

    public NotificationController(TicketNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/tickets/{id}/notifications")
    public ResponseEntity<List<NotificationResponse>> findByTicket(@PathVariable("id") Long ticketId) {
        return ResponseEntity.ok(notificationService.findByTicket(ticketId));
    }

    @PostMapping("/tickets/{id}/notifications")
    public ResponseEntity<NotificationResponse> sendManual(@PathVariable("id") Long ticketId,
                                                           @Valid @RequestBody ManualNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.sendManual(ticketId, request));
    }

    @GetMapping("/customers/{id}/notification-preferences")
    public ResponseEntity<NotificationPreferenceResponse> findPreferences(@PathVariable("id") Long customerId) {
        return ResponseEntity.ok(notificationService.findPreferences(customerId));
    }

    @PutMapping("/customers/{id}/notification-preferences")
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(@PathVariable("id") Long customerId,
                                                                            @Valid @RequestBody NotificationPreferenceRequest request) {
        return ResponseEntity.ok(notificationService.updatePreferences(customerId, request));
    }

    @GetMapping("/notifications/status")
    public ResponseEntity<NotificationServiceStatus> status() {
        return ResponseEntity.ok(notificationService.status());
    }
}
