package com.pb.crm.team.infrastructure.persistence;

import com.pb.crm.commons.audit.ArchivableEntity;
import com.pb.crm.team.domain.salesrep.SalesRole;
import com.pb.crm.team.domain.salesrep.SalesTeam;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@Entity
@Audited
@Table(name = "sales_reps")
public class SalesRepJpaEntity extends ArchivableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sales_reps_seq")
    @SequenceGenerator(name = "sales_reps_seq", sequenceName = "sales_reps_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SalesTeam team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SalesRole role;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "monthly_quota", precision = 15, scale = 2)
    private BigDecimal monthlyQuota;

    @Column(nullable = false)
    private boolean active;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public SalesTeam getTeam() {
        return team;
    }

    public void setTeam(SalesTeam team) {
        this.team = team;
    }

    public SalesRole getRole() {
        return role;
    }

    public void setRole(SalesRole role) {
        this.role = role;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public BigDecimal getMonthlyQuota() {
        return monthlyQuota;
    }

    public void setMonthlyQuota(BigDecimal monthlyQuota) {
        this.monthlyQuota = monthlyQuota;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
