package com.pb.crm.notification.api;

import com.pb.crm.notification.application.preference.PreferenceService;
import com.pb.crm.notification.domain.preference.NotificationPreference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification-preferences")
public class PreferenceController {

    public record PreferenceRequest(@NotNull(message = "emailEnabled e obrigatorio") Boolean emailEnabled) {
    }

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping("/{salesRepId}")
    public ResponseEntity<NotificationPreference> find(@PathVariable Long salesRepId) {
        return ResponseEntity.ok(preferenceService.find(salesRepId));
    }

    @PutMapping("/{salesRepId}")
    public ResponseEntity<NotificationPreference> update(@PathVariable Long salesRepId,
                                                         @Valid @RequestBody PreferenceRequest request) {
        return ResponseEntity.ok(preferenceService.update(salesRepId, request.emailEnabled()));
    }
}
