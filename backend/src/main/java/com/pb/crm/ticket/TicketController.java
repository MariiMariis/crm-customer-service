package com.pb.crm.ticket;

import com.pb.crm.ticket.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
        return ResponseEntity.ok(ticketService.changeStatus(id, request.status()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> findAll(@RequestParam(required = false) TicketStatus status) {
        List<TicketResponse> tickets = status != null
                ? ticketService.findByStatus(status)
                : ticketService.findAll();
        return ResponseEntity.ok(tickets);
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
