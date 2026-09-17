package com.pb.crm.ticket;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Audited
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "interactions",
        indexes = @Index(name = "idx_interactions_ticket", columnList = "ticket_id, created_at")
)
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "interactions_seq")
    @SequenceGenerator(name = "interactions_seq", sequenceName = "interactions_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, foreignKey = @ForeignKey(name = "fk_interactions_ticket"))
    private Ticket ticket;

    @Column(nullable = false, length = 120)
    private String author;

    @Column(nullable = false, length = 2000)
    private String message;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Interaction() {
    }

    Interaction(Ticket ticket, String author, String message) {
        this.ticket = ticket;
        this.author = author;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
