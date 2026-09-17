package com.pb.notification.preference;

import com.pb.notification.preference.dto.NotificationPreferenceRequest;
import com.pb.notification.preference.dto.NotificationPreferenceResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification-preferences")
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    public NotificationPreferenceController(NotificationPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<NotificationPreferenceResponse> findByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(preferenceService.findByCustomer(customerId));
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<NotificationPreferenceResponse> update(@PathVariable Long customerId,
                                                                 @Valid @RequestBody NotificationPreferenceRequest request) {
        return ResponseEntity.ok(preferenceService.update(customerId, request));
    }
}
