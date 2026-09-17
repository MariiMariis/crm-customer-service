package com.pb.crm.ticket;

import com.pb.crm.audit.RevisionResponse;
import com.pb.crm.common.PageResponse;
import com.pb.crm.ticket.dto.InteractionRequest;
import com.pb.crm.ticket.dto.InteractionResponse;
import com.pb.crm.ticket.dto.TicketRequest;
import com.pb.crm.ticket.dto.TicketResponse;
import com.pb.crm.ticket.dto.TicketStatsResponse;
import com.pb.crm.ticket.dto.TicketStatusHistoryResponse;
import com.pb.crm.ticket.dto.TicketStatusRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody TicketRequest request) {
        TicketResponse created = ticketService.create(request);
        return ResponseEntity.created(URI.create("/api/tickets/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> update(@PathVariable Long id, @Valid @RequestBody TicketRequest request) {
        return ResponseEntity.ok(ticketService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> changeStatus(@PathVariable Long id, @Valid @RequestBody TicketStatusRequest request) {
        return ResponseEntity.ok(ticketService.changeStatus(id, request.status(), request.reason()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> findAll(@RequestParam(required = false) TicketStatus status,
                                                        @RequestParam(required = false) Long customerId) {
        List<TicketResponse> tickets;
        if (status != null && customerId != null) {
            tickets = ticketService.findByStatusAndCustomer(status, customerId);
        } else if (status != null) {
            tickets = ticketService.findByStatus(status);
        } else if (customerId != null) {
            tickets = ticketService.findByCustomer(customerId);
        } else {
            tickets = ticketService.findAll();
        }
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<TicketResponse>> search(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        TicketFilter filter = new TicketFilter(status, priority, customerId, agentId, subject, createdFrom, createdTo);
        return ResponseEntity.ok(ticketService.search(filter, pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<TicketStatsResponse> stats() {
        return ResponseEntity.ok(ticketService.stats());
    }

    @GetMapping("/{id}/status-history")
    public ResponseEntity<List<TicketStatusHistoryResponse>> statusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.findStatusHistory(id));
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity<List<RevisionResponse<TicketResponse>>> revisions(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.findRevisions(id));
    }

    @PostMapping("/{id}/interactions")
    public ResponseEntity<InteractionResponse> addInteraction(@PathVariable("id") Long ticketId,
                                                              @Valid @RequestBody InteractionRequest request) {
        InteractionResponse created = ticketService.addInteraction(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
