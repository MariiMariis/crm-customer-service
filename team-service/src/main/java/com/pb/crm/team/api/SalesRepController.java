package com.pb.crm.team.api;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.web.PageResponse;
import com.pb.crm.team.application.salesrep.SalesRepService;
import com.pb.crm.team.application.salesrep.dto.SalesRepRequest;
import com.pb.crm.team.application.salesrep.dto.SalesRepResponse;
import com.pb.crm.team.domain.salesrep.SalesRepCriteria;
import com.pb.crm.team.domain.salesrep.SalesRole;
import com.pb.crm.team.domain.salesrep.SalesTeam;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/sales-reps")
public class SalesRepController {

    private final SalesRepService service;

    public SalesRepController(SalesRepService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SalesRepResponse> create(@Valid @RequestBody SalesRepRequest request) {
        SalesRepResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/sales-reps/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalesRepResponse> update(@PathVariable Long id, @Valid @RequestBody SalesRepRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesRepResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<SalesRepResponse>> search(
            @RequestParam(name = "q", required = false) String term,
            @RequestParam(required = false) SalesTeam team,
            @RequestParam(required = false) SalesRole role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long managerId,
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        SalesRepCriteria criteria = new SalesRepCriteria(term, team, role, active, managerId, includeArchived);
        return ResponseEntity.ok(PageResponse.from(service.search(criteria, new PageQuery(page, size))));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<SalesRepResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(service.activate(id));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<SalesRepResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(service.deactivate(id));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<SalesRepResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(service.archive(id));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<SalesRepResponse> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity<List<AuditRevision<SalesRepResponse>>> revisions(@PathVariable Long id) {
        return ResponseEntity.ok(service.findRevisions(id));
    }
}
