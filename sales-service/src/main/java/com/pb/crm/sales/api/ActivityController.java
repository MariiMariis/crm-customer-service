package com.pb.crm.sales.api;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.web.PageResponse;
import com.pb.crm.sales.application.activity.ActivityService;
import com.pb.crm.sales.application.activity.dto.ActivityActions;
import com.pb.crm.sales.application.activity.dto.ActivityRequest;
import com.pb.crm.sales.application.activity.dto.ActivityResponse;
import com.pb.crm.sales.application.activity.dto.ActivitySummaryResponse;
import com.pb.crm.sales.domain.activity.ActivityCriteria;
import com.pb.crm.sales.domain.activity.ActivityStatus;
import com.pb.crm.sales.domain.activity.ActivityType;
import com.pb.crm.sales.domain.activity.RelatedType;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService service;

    public ActivityController(ActivityService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> create(@Valid @RequestBody ActivityRequest request) {
        ActivityResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/activities/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse> update(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ActivityResponse>> search(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) RelatedType relatedType,
            @RequestParam(required = false) Long relatedId,
            @RequestParam(required = false) ActivityType type,
            @RequestParam(required = false) ActivityStatus status,
            @RequestParam(defaultValue = "false") boolean overdue,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dueFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dueTo,
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        ActivityCriteria criteria = new ActivityCriteria(ownerId, relatedType, relatedId, type, status, overdue,
                dueFrom, dueTo, null, includeArchived);
        return ResponseEntity.ok(PageResponse.from(service.search(criteria, new PageQuery(page, pageSize))));
    }

    @GetMapping("/agenda")
    public ResponseEntity<List<ActivityResponse>> agenda(
            @RequestParam(required = false) Long ownerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ResponseEntity.ok(service.agenda(ownerId, from, to));
    }

    @GetMapping("/summary")
    public ResponseEntity<ActivitySummaryResponse> summary(@RequestParam(required = false) Long ownerId) {
        return ResponseEntity.ok(service.summary(ownerId));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ActivityResponse> complete(@PathVariable Long id, @Valid @RequestBody ActivityActions.Complete request) {
        return ResponseEntity.ok(service.complete(id, request.outcome(), request.durationMinutes()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ActivityResponse> cancel(@PathVariable Long id, @Valid @RequestBody ActivityActions.Cancel request) {
        return ResponseEntity.ok(service.cancel(id, request.reason()));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<ActivityResponse> reopen(@PathVariable Long id) {
        return ResponseEntity.ok(service.reopen(id));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ActivityResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(service.archive(id));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<ActivityResponse> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity<List<AuditRevision<ActivityResponse>>> revisions(@PathVariable Long id) {
        return ResponseEntity.ok(service.findRevisions(id));
    }
}
