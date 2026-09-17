package com.pb.crm.agent;

import com.pb.crm.agent.dto.AgentRequest;
import com.pb.crm.agent.dto.AgentResponse;
import com.pb.crm.audit.RevisionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<AgentResponse>> findAll(@RequestParam(name = "active", required = false) Boolean active) {
        List<AgentResponse> agents = Boolean.TRUE.equals(active) ? agentService.findActive() : agentService.findAll();
        return ResponseEntity.ok(agents);
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity<List<RevisionResponse<AgentResponse>>> findRevisions(@PathVariable Long id) {
        return ResponseEntity.ok(agentService.findRevisions(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        agentService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
