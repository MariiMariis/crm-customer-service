package com.pb.crm.sales.api;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.web.PageResponse;
import com.pb.crm.sales.application.lead.LeadService;
import com.pb.crm.sales.application.lead.dto.AssignLeadRequest;
import com.pb.crm.sales.application.lead.dto.ConvertLeadRequest;
import com.pb.crm.sales.application.lead.dto.DisqualifyLeadRequest;
import com.pb.crm.sales.application.lead.dto.LeadRequest;
import com.pb.crm.sales.application.lead.dto.LeadResponse;
import com.pb.crm.sales.application.lead.dto.LeadStatsResponse;
import com.pb.crm.sales.domain.lead.LeadCriteria;
import com.pb.crm.sales.domain.lead.LeadSource;
import com.pb.crm.sales.domain.lead.LeadStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService service;

    public LeadController(LeadService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<LeadResponse> create(@Valid @RequestBody LeadRequest request) {
        LeadResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/leads/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeadResponse> update(@PathVariable Long id, @Valid @RequestBody LeadRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<LeadResponse>> search(
            @RequestParam(name = "q", required = false) String term,
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) LeadSource source,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(defaultValue = "false") boolean unassigned,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        LeadCriteria criteria = new LeadCriteria(term, status, source, ownerId, unassigned, minScore, includeArchived);
        return ResponseEntity.ok(PageResponse.from(service.search(criteria, new PageQuery(page, pageSize))));
    }

    @GetMapping("/stats")
    public ResponseEntity<LeadStatsResponse> stats() {
        return ResponseEntity.ok(service.stats());
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<LeadResponse> assign(@PathVariable Long id, @Valid @RequestBody AssignLeadRequest request) {
        return ResponseEntity.ok(service.assign(id, request.ownerId()));
    }

    @PostMapping("/{id}/contacted")
    public ResponseEntity<LeadResponse> markContacted(@PathVariable Long id) {
        return ResponseEntity.ok(service.markContacted(id));
    }

    @PostMapping("/{id}/qualify")
    public ResponseEntity<LeadResponse> qualify(@PathVariable Long id) {
        return ResponseEntity.ok(service.qualify(id));
    }

    @PostMapping("/{id}/disqualify")
    public ResponseEntity<LeadResponse> disqualify(@PathVariable Long id, @Valid @RequestBody DisqualifyLeadRequest request) {
        return ResponseEntity.ok(service.disqualify(id, request.reason()));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<LeadResponse> reopen(@PathVariable Long id) {
        return ResponseEntity.ok(service.reopen(id));
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<LeadResponse> convert(@PathVariable Long id, @Valid @RequestBody ConvertLeadRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.requestConversion(id, request));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<LeadResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(service.archive(id));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<LeadResponse> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity<List<AuditRevision<LeadResponse>>> revisions(@PathVariable Long id) {
        return ResponseEntity.ok(service.findRevisions(id));
    }
}
