package com.pb.crm.notification;

import com.pb.crm.notification.dto.NotificationPreferenceRequest;
import com.pb.crm.notification.dto.NotificationPreferenceResponse;
import com.pb.crm.notification.dto.NotificationRequest;
import com.pb.crm.notification.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = NotificationClient.SERVICE_ID)
public interface NotificationClient {

    String SERVICE_ID = "notification-service";

    @PostMapping("/api/notifications")
    NotificationResponse create(@RequestBody NotificationRequest request);

    @GetMapping("/api/notifications")
    List<NotificationResponse> findByTicket(@RequestParam("ticketId") Long ticketId);

    @GetMapping("/api/notification-preferences/{customerId}")
    NotificationPreferenceResponse findPreferences(@PathVariable("customerId") Long customerId);

    @PutMapping("/api/notification-preferences/{customerId}")
    NotificationPreferenceResponse updatePreferences(@PathVariable("customerId") Long customerId,
                                                     @RequestBody NotificationPreferenceRequest request);

    @GetMapping("/actuator/health")
    Map<String, Object> health();
}
