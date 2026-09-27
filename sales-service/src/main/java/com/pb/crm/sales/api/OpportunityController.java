package com.pb.crm.sales.api;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.web.PageResponse;
import com.pb.crm.sales.application.opportunity.OpportunityService;
import com.pb.crm.sales.application.opportunity.dto.OpportunityActions;
import com.pb.crm.sales.application.opportunity.dto.OpportunityItemRequest;
import com.pb.crm.sales.application.opportunity.dto.OpportunityRequest;
import com.pb.crm.sales.application.opportunity.dto.OpportunityResponse;
import com.pb.crm.sales.application.opportunity.dto.PipelineResponse;
import com.pb.crm.sales.domain.opportunity.DiscountApprovalStatus;
import com.pb.crm.sales.domain.opportunity.OpportunityCriteria;
import com.pb.crm.sales.domain.opportunity.OpportunityStage;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final OpportunityService service;

    public OpportunityController(OpportunityService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OpportunityResponse> create(@Valid @RequestBody OpportunityRequest request) {
        OpportunityResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/opportunities/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OpportunityResponse> update(@PathVariable Long id, @Valid @RequestBody OpportunityRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OpportunityResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<OpportunityResponse>> search(
            @RequestParam(name = "q", required = false) String term,
            @RequestParam(required = false) OpportunityStage stage,
            @RequestParam(required = false) Boolean open,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) DiscountApprovalStatus discountApproval,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closingFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closingTo,
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        OpportunityCriteria criteria = new OpportunityCriteria(term, stage, open, companyId, ownerId, discountApproval,
                closingFrom, closingTo, includeArchived);
        return ResponseEntity.ok(PageResponse.from(service.search(criteria, new PageQuery(page, pageSize))));
    }

    @GetMapping("/pipeline")
    public ResponseEntity<PipelineResponse> pipeline(@RequestParam(required = false) Long ownerId) {
        return ResponseEntity.ok(service.pipeline(ownerId));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<OpportunityResponse> addItem(@PathVariable Long id, @Valid @RequestBody OpportunityItemRequest request) {
        return ResponseEntity.ok(service.addItem(id, request));
    }

    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<OpportunityResponse> changeItem(@PathVariable Long id,
                                                          @PathVariable Long itemId,
                                                          @Valid @RequestBody OpportunityItemRequest request) {
        return ResponseEntity.ok(service.changeItem(id, itemId, request));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<OpportunityResponse> removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        return ResponseEntity.ok(service.removeItem(id, itemId));
    }

    @PostMapping("/{id}/stage")
    public ResponseEntity<OpportunityResponse> moveTo(@PathVariable Long id,
                                                      @Valid @RequestBody OpportunityActions.ChangeStage request) {
        return ResponseEntity.ok(service.moveTo(id, request.stage()));
    }

    @PostMapping("/{id}/probability")
    public ResponseEntity<OpportunityResponse> adjustProbability(@PathVariable Long id,
                                                                 @Valid @RequestBody OpportunityActions.AdjustProbability request) {
        return ResponseEntity.ok(service.adjustProbability(id, request.probability()));
    }

    @PostMapping("/{id}/win")
    public ResponseEntity<OpportunityResponse> win(@PathVariable Long id) {
        return ResponseEntity.ok(service.win(id));
    }

    @PostMapping("/{id}/lose")
    public ResponseEntity<OpportunityResponse> lose(@PathVariable Long id, @Valid @RequestBody OpportunityActions.Lose request) {
        return ResponseEntity.ok(service.lose(id, request.reason()));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<OpportunityResponse> reopen(@PathVariable Long id) {
        return ResponseEntity.ok(service.reopen(id));
    }

    @PostMapping("/{id}/discount-decision")
    public ResponseEntity<OpportunityResponse> decideDiscount(@PathVariable Long id,
                                                              @Valid @RequestBody OpportunityActions.DiscountDecision request) {
        return ResponseEntity.ok(service.decideDiscount(id, request.approverId(), request.approved(), request.comment()));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<OpportunityResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(service.archive(id));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<OpportunityResponse> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity<List<AuditRevision<OpportunityResponse>>> revisions(@PathVariable Long id) {
        return ResponseEntity.ok(service.findRevisions(id));
    }
}
