package com.pb.crm.gateway.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform")
public class PlatformHealthController {

    private final PlatformHealthService healthService;

    public PlatformHealthController(PlatformHealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/health")
    public ResponseEntity<PlatformHealthService.PlatformHealth> health() {
        return ResponseEntity.ok(healthService.check());
    }
}
