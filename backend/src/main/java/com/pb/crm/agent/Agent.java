package com.pb.crm.agent;

import com.pb.crm.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;

@Entity
@Audited
@AuditOverride(forClass = AuditableEntity.class)
@Table(
        name = "agents",
        uniqueConstraints = @UniqueConstraint(name = "uk_agents_email", columnNames = "email"),
        indexes = {
                @Index(name = "idx_agents_department", columnList = "department"),
                @Index(name = "idx_agents_active", columnList = "active")
        }
)
public class Agent extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "agents_seq")
    @SequenceGenerator(name = "agents_seq", sequenceName = "agents_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(length = 80)
    private String department;

    @Column(nullable = false)
    private boolean active = true;

    protected Agent() {
    }

    public Agent(String name, String email, String department) {
        this.name = name;
        this.email = normalizeEmail(email);
        this.department = department;
    }

    public void update(String name, String email, String department, boolean active) {
        this.name = name;
        this.email = normalizeEmail(email);
        this.department = department;
        this.active = active;
        touch();
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public boolean isActive() {
        return active;
    }
}
