package com.pb.crm.team.domain.salesrep;

import com.pb.crm.commons.domain.AggregateRoot;
import com.pb.crm.commons.domain.AuditInfo;
import com.pb.crm.commons.error.BusinessRuleException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class SalesRep extends AggregateRoot {

    public static final String REGISTERED = "salesrep.registered";
    public static final String UPDATED = "salesrep.updated";
    public static final String STATUS_CHANGED = "salesrep.status-changed";

    private String name;
    private String email;
    private String phone;
    private SalesTeam team;
    private SalesRole role;
    private Long managerId;
    private BigDecimal monthlyQuota;
    private boolean active;

    private SalesRep() {
    }

    public static SalesRep register(String name,
                                    String email,
                                    String phone,
                                    SalesTeam team,
                                    SalesRole role,
                                    BigDecimal monthlyQuota,
                                    SalesRep manager) {
        SalesRep salesRep = new SalesRep();
        salesRep.active = true;
        salesRep.applyProfile(name, email, phone, team, role, monthlyQuota);
        salesRep.assignManager(manager);
        salesRep.recordEvent(REGISTERED);
        return salesRep;
    }

    public static SalesRep rehydrate(Long id,
                                     String name,
                                     String email,
                                     String phone,
                                     SalesTeam team,
                                     SalesRole role,
                                     Long managerId,
                                     BigDecimal monthlyQuota,
                                     boolean active,
                                     boolean archived,
                                     Instant archivedAt,
                                     AuditInfo audit) {
        SalesRep salesRep = new SalesRep();
        salesRep.rehydrateBase(id, audit, archived, archivedAt);
        salesRep.name = name;
        salesRep.email = email;
        salesRep.phone = phone;
        salesRep.team = team;
        salesRep.role = role;
        salesRep.managerId = managerId;
        salesRep.monthlyQuota = monthlyQuota;
        salesRep.active = active;
        return salesRep;
    }

    public void update(String name,
                       String email,
                       String phone,
                       SalesTeam team,
                       SalesRole role,
                       BigDecimal monthlyQuota,
                       SalesRep manager) {
        assertNotArchived();
        applyProfile(name, email, phone, team, role, monthlyQuota);
        assignManager(manager);
        recordEvent(UPDATED);
    }

    public void activate() {
        assertNotArchived();
        if (active) {
            throw new BusinessRuleException("o vendedor ja esta ativo");
        }
        active = true;
        recordEvent(STATUS_CHANGED);
    }

    public void deactivate() {
        assertNotArchived();
        if (!active) {
            throw new BusinessRuleException("o vendedor ja esta inativo");
        }
        active = false;
        recordEvent(STATUS_CHANGED);
    }

    @Override
    public void archive() {
        super.archive();
        active = false;
        recordEvent(STATUS_CHANGED);
    }

    @Override
    public void restore() {
        super.restore();
        recordEvent(STATUS_CHANGED);
    }

    public boolean canOwnRecords() {
        return active && !isArchived();
    }

    public boolean isManager() {
        return role == SalesRole.MANAGER;
    }

    private void applyProfile(String name,
                              String email,
                              String phone,
                              SalesTeam team,
                              SalesRole role,
                              BigDecimal monthlyQuota) {
        this.name = requireText(name, "nome");
        this.email = normalizeEmail(email);
        this.phone = blankToNull(phone);
        this.team = requireValue(team, "equipe");
        this.role = requireValue(role, "papel");
        if (monthlyQuota != null && monthlyQuota.signum() < 0) {
            throw new BusinessRuleException("a meta mensal nao pode ser negativa");
        }
        if (role == SalesRole.REP && monthlyQuota == null) {
            throw new BusinessRuleException("a meta mensal e obrigatoria para vendedores");
        }
        this.monthlyQuota = monthlyQuota;
    }

    private void assignManager(SalesRep manager) {
        if (manager == null) {
            this.managerId = null;
            return;
        }
        if (!isNew() && Objects.equals(manager.getId(), getId())) {
            throw new BusinessRuleException("um vendedor nao pode ser gestor de si mesmo");
        }
        if (!manager.isManager()) {
            throw new BusinessRuleException("o gestor informado nao possui o papel de gestor");
        }
        if (!manager.canOwnRecords()) {
            throw new BusinessRuleException("o gestor informado esta inativo ou arquivado");
        }
        this.managerId = manager.getId();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " e obrigatorio");
        }
        return value.trim();
    }

    private static <T> T requireValue(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " e obrigatorio");
        }
        return value;
    }

    private static String normalizeEmail(String email) {
        return requireText(email, "email").toLowerCase();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public SalesTeam getTeam() {
        return team;
    }

    public SalesRole getRole() {
        return role;
    }

    public Long getManagerId() {
        return managerId;
    }

    public BigDecimal getMonthlyQuota() {
        return monthlyQuota;
    }

    public boolean isActive() {
        return active;
    }
}
