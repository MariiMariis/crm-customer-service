package com.pb.crm.commons.messaging.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messaging")
public class MessagingController {

    private final MessagingOperations operations;

    public MessagingController(MessagingOperations operations) {
        this.operations = operations;
    }

    @GetMapping("/status")
    public ResponseEntity<MessagingOperations.Status> status() {
        return ResponseEntity.ok(operations.status());
    }

    @GetMapping("/outbox")
    public ResponseEntity<List<MessagingOperations.OutboxEntry>> recentOutbox() {
        return ResponseEntity.ok(operations.recentOutbox());
    }

    @PostMapping("/dead-letters/{queue}/replay")
    public ResponseEntity<MessagingOperations.ReplayResult> replay(@PathVariable String queue,
                                                                   @RequestParam(defaultValue = "50") int max) {
        return ResponseEntity.ok(operations.replayDeadLetters(queue, Math.min(Math.max(max, 1), 500)));
    }
}
