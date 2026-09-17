package com.pb.crm.agent;

import com.pb.crm.agent.dto.AgentRequest;
import com.pb.crm.agent.dto.AgentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping
    public ResponseEntity<AgentResponse> create(@Valid @RequestBody AgentRequest request) {
        AgentResponse created = agentService.create(request);
        return ResponseEntity.created(URI.create("/api/agents/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgentResponse> update(@PathVariable Long id, @Valid @RequestBody AgentRequest request) {
        return ResponseEntity.ok(agentService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(agentService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<AgentResponse>> findAll() {
        return ResponseEntity.ok(agentService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        agentService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
