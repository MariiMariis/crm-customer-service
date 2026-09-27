package com.pb.crm.team.domain;

import com.pb.crm.commons.domain.AuditInfo;
import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.team.domain.salesrep.SalesRep;
import com.pb.crm.team.domain.salesrep.SalesRole;
import com.pb.crm.team.domain.salesrep.SalesTeam;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalesRepTest {

    private static SalesRep persistedManager(Long id, boolean active, boolean archived) {
        return SalesRep.rehydrate(id, "Marina Costa", "marina@pbtech.com.br", null, SalesTeam.FIELD_SALES,
                SalesRole.MANAGER, null, null, active, archived, null, AuditInfo.empty());
    }

    private static SalesRep persistedRep(Long id) {
        return SalesRep.rehydrate(id, "Joao Lima", "joao@pbtech.com.br", null, SalesTeam.INSIDE_SALES,
                SalesRole.REP, null, new BigDecimal("50000.00"), true, false, null, AuditInfo.empty());
    }

    @Test
    void registerNormalizesDataAndStartsActive() {
        SalesRep rep = SalesRep.register("  Joao Lima ", " JOAO@PBTech.com.br ", " ", SalesTeam.INSIDE_SALES,
                SalesRole.REP, new BigDecimal("80000.00"), persistedManager(1L, true, false));

        assertThat(rep.getName()).isEqualTo("Joao Lima");
        assertThat(rep.getEmail()).isEqualTo("joao@pbtech.com.br");
        assertThat(rep.getPhone()).isNull();
        assertThat(rep.getManagerId()).isEqualTo(1L);
        assertThat(rep.isActive()).isTrue();
        assertThat(rep.canOwnRecords()).isTrue();
    }

    @Test
    void repRequiresNonNegativeQuotaButManagerDoesNot() {
        assertThatThrownBy(() -> SalesRep.register("Joao", "joao@pbtech.com.br", null, SalesTeam.INSIDE_SALES,
                SalesRole.REP, null, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("meta mensal");

        assertThatThrownBy(() -> SalesRep.register("Joao", "joao@pbtech.com.br", null, SalesTeam.INSIDE_SALES,
                SalesRole.REP, new BigDecimal("-1"), null))
                .isInstanceOf(BusinessRuleException.class);

        SalesRep manager = SalesRep.register("Marina", "marina@pbtech.com.br", null, SalesTeam.FIELD_SALES,
                SalesRole.MANAGER, null, null);
        assertThat(manager.isManager()).isTrue();
    }

    @Test
    void managerMustHaveManagerRoleAndBeAbleToOwnRecords() {
        assertThatThrownBy(() -> SalesRep.register("Ana", "ana@pbtech.com.br", null, SalesTeam.INSIDE_SALES,
                SalesRole.REP, BigDecimal.TEN, persistedRep(2L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("papel de gestor");

        assertThatThrownBy(() -> SalesRep.register("Ana", "ana@pbtech.com.br", null, SalesTeam.INSIDE_SALES,
                SalesRole.REP, BigDecimal.TEN, persistedManager(1L, false, false)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inativo ou arquivado");
    }

    @Test
    void salesRepCannotManageItself() {
        SalesRep manager = persistedManager(1L, true, false);

        assertThatThrownBy(() -> manager.update("Marina", "marina@pbtech.com.br", null, SalesTeam.FIELD_SALES,
                SalesRole.MANAGER, null, manager))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("si mesmo");
    }

    @Test
    void archiveDeactivatesAndBlocksChangesUntilRestore() {
        SalesRep rep = persistedRep(2L);

        rep.archive();
        assertThat(rep.isActive()).isFalse();
        assertThat(rep.canOwnRecords()).isFalse();
        assertThatThrownBy(rep::activate).isInstanceOf(BusinessRuleException.class);

        rep.restore();
        assertThat(rep.isArchived()).isFalse();
        assertThat(rep.isActive()).isFalse();
        rep.activate();
        assertThat(rep.isActive()).isTrue();
    }

    @Test
    void activateAndDeactivateRejectRepeatedTransitions() {
        SalesRep rep = persistedRep(2L);

        assertThatThrownBy(rep::activate).isInstanceOf(BusinessRuleException.class);
        rep.deactivate();
        assertThatThrownBy(rep::deactivate).isInstanceOf(BusinessRuleException.class);
    }
}
