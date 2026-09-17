package com.pb.crm.ticket;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<Interaction> interactions = new ArrayList<>();

    protected Ticket() {
        // exigido pelo JPA
    }

    public Ticket(String subject, String description, TicketPriority priority, Long customerId, Long agentId) {
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.customerId = customerId;
        this.agentId = agentId;
        this.status = TicketStatus.OPEN;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(String subject, String description, TicketPriority priority, Long agentId) {
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.agentId = agentId;
        touch();
    }

    public void changeStatus(TicketStatus newStatus) {
        this.status = newStatus;
        touch();
    }

    public Interaction addInteraction(String author, String message) {
        Interaction interaction = new Interaction(this, author, message);
        this.interactions.add(interaction);
        touch();
        return interaction;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getAgentId() {
        return agentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<Interaction> getInteractions() {
        return Collections.unmodifiableList(interactions);
    }
}
