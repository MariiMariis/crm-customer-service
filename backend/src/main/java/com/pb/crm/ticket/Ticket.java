package com.pb.crm.ticket;

import com.pb.crm.agent.Agent;
import com.pb.crm.audit.AuditableEntity;
import com.pb.crm.common.BusinessRuleException;
import com.pb.crm.customer.Customer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Audited
@AuditOverride(forClass = AuditableEntity.class)
@Table(
        name = "tickets",
        indexes = {
                @Index(name = "idx_tickets_status", columnList = "status"),
                @Index(name = "idx_tickets_priority_status", columnList = "priority, status"),
                @Index(name = "idx_tickets_customer", columnList = "customer_id"),
                @Index(name = "idx_tickets_agent", columnList = "agent_id"),
                @Index(name = "idx_tickets_created_at", columnList = "created_at")
        }
)
public class Ticket extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tickets_seq")
    @SequenceGenerator(name = "tickets_seq", sequenceName = "tickets_seq", allocationSize = 50)
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tickets_customer"))
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", foreignKey = @ForeignKey(name = "fk_tickets_agent"))
    private Agent agent;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC, id ASC")
    private List<Interaction> interactions = new ArrayList<>();

    @NotAudited
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("changedAt ASC, id ASC")
    private List<TicketStatusHistory> statusHistory = new ArrayList<>();

    protected Ticket() {
    }

    public Ticket(String subject, String description, TicketPriority priority, Customer customer, Agent agent) {
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.customer = customer;
        this.agent = agent;
        this.status = TicketStatus.OPEN;
        this.statusHistory.add(new TicketStatusHistory(this, null, TicketStatus.OPEN, "abertura do ticket"));
    }

    public void update(String subject, String description, TicketPriority priority, Agent agent) {
        assertNotClosed("nao e possivel alterar um ticket fechado");
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.agent = agent;
        touch();
    }

    public TicketStatusHistory changeStatus(TicketStatus newStatus, String reason) {
        if (newStatus == this.status) {
            throw new BusinessRuleException("o ticket ja esta no status " + newStatus);
        }
        if (!this.status.canTransitionTo(newStatus)) {
            throw new BusinessRuleException(
                    "transicao de status invalida: %s -> %s (permitidas a partir de %s: %s)"
                            .formatted(this.status, newStatus, this.status, this.status.allowedTransitions()));
        }
        TicketStatus previous = this.status;
        this.status = newStatus;
        Instant now = Instant.now();
        switch (newStatus) {
            case RESOLVED -> this.resolvedAt = now;
            case CLOSED -> this.closedAt = now;
            case OPEN, IN_PROGRESS -> {
                this.resolvedAt = null;
                this.closedAt = null;
            }
        }
        TicketStatusHistory entry = new TicketStatusHistory(this, previous, newStatus, reason);
        this.statusHistory.add(entry);
        touch();
        return entry;
    }

    public Interaction addInteraction(String author, String message) {
        assertNotClosed("nao e possivel registrar interacoes em um ticket fechado");
        Interaction interaction = new Interaction(this, author, message);
        this.interactions.add(interaction);
        touch();
        return interaction;
    }

    private void assertNotClosed(String message) {
        if (this.status.isTerminal()) {
            throw new BusinessRuleException(message);
        }
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

    public Customer getCustomer() {
        return customer;
    }

    public Agent getAgent() {
        return agent;
    }

    public Long getCustomerId() {
        return customer == null ? null : customer.getId();
    }

    public Long getAgentId() {
        return agent == null ? null : agent.getId();
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public List<Interaction> getInteractions() {
        return Collections.unmodifiableList(interactions);
    }

    public List<TicketStatusHistory> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }
}
