package com.pb.crm.accounts.api;

import com.pb.crm.accounts.application.contact.ContactService;
import com.pb.crm.accounts.application.contact.dto.ContactRequest;
import com.pb.crm.accounts.application.contact.dto.ContactResponse;
import com.pb.crm.accounts.domain.contact.ContactCriteria;
import com.pb.crm.accounts.domain.contact.DecisionRole;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.web.PageResponse;
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
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService service;

    public ContactController(ContactService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ContactResponse> create(@Valid @RequestBody ContactRequest request) {
        ContactResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/contacts/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> update(@PathVariable Long id, @Valid @RequestBody ContactRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ContactResponse>> search(
            @RequestParam(name = "q", required = false) String term,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) DecisionRole decisionRole,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        ContactCriteria criteria = new ContactCriteria(term, companyId, decisionRole, active, includeArchived);
        return ResponseEntity.ok(PageResponse.from(service.search(criteria, new PageQuery(page, pageSize))));
    }

    @PostMapping("/{id}/make-primary")
    public ResponseEntity<ContactResponse> makePrimary(@PathVariable Long id) {
        return ResponseEntity.ok(service.makePrimary(id));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ContactResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(service.archive(id));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<ContactResponse> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity<List<AuditRevision<ContactResponse>>> revisions(@PathVariable Long id) {
        return ResponseEntity.ok(service.findRevisions(id));
    }
}
