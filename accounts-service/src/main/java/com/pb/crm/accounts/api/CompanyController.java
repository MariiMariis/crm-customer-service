package com.pb.crm.accounts.api;

import com.pb.crm.accounts.application.company.CompanyService;
import com.pb.crm.accounts.application.company.dto.CompanyRequest;
import com.pb.crm.accounts.application.company.dto.CompanyResponse;
import com.pb.crm.accounts.domain.company.BrazilianState;
import com.pb.crm.accounts.domain.company.CompanyCriteria;
import com.pb.crm.accounts.domain.company.CompanySize;
import com.pb.crm.accounts.domain.company.CompanyType;
import com.pb.crm.accounts.domain.company.Industry;
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
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/companies/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> update(@PathVariable Long id, @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CompanyResponse>> search(
            @RequestParam(name = "q", required = false) String term,
            @RequestParam(required = false) Industry industry,
            @RequestParam(required = false) CompanySize size,
            @RequestParam(required = false) CompanyType type,
            @RequestParam(required = false) BrazilianState state,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        CompanyCriteria criteria = new CompanyCriteria(term, industry, size, type, state, ownerId, includeArchived);
        return ResponseEntity.ok(PageResponse.from(service.search(criteria, new PageQuery(page, pageSize))));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<CompanyResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(service.archive(id));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<CompanyResponse> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity<List<AuditRevision<CompanyResponse>>> revisions(@PathVariable Long id) {
        return ResponseEntity.ok(service.findRevisions(id));
    }
}
