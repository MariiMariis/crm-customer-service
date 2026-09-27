package com.pb.crm.sales.domain;

import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.sales.domain.opportunity.BillingType;
import com.pb.crm.sales.domain.opportunity.DiscountApprovalStatus;
import com.pb.crm.sales.domain.opportunity.Opportunity;
import com.pb.crm.sales.domain.opportunity.OpportunityDetails;
import com.pb.crm.sales.domain.opportunity.OpportunityStage;
import com.pb.crm.sales.domain.reference.CompanyRef;
import com.pb.crm.sales.domain.reference.ContactRef;
import com.pb.crm.sales.domain.reference.ProductRef;
import com.pb.crm.sales.domain.reference.SalesRepRef;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpportunityTest {

    private static final CompanyRef COMPANY = new CompanyRef(10L, "Hospital Santa Clara", "11222333000181", 1L, false);
    private static final SalesRepRef MANAGER = new SalesRepRef(100L, "Marina", null, null, true, false);
    private static final SalesRepRef OWNER = new SalesRepRef(1L, "Ana", null, 100L, true, false);
    private static final ProductRef NOTEBOOK = new ProductRef(5L, "HW-NBK-000001", "Notebook Latitude", "HARDWARE",
            BillingType.ONE_TIME, new BigDecimal("8500.00"), new BigDecimal("8.00"), true, false);
    private static final ProductRef M365 = new ProductRef(6L, "SW-PRD-000002", "Microsoft 365 anual", "SOFTWARE",
            BillingType.ANNUAL, new BigDecimal("900.00"), new BigDecimal("5.00"), true, false);

    private static OpportunityDetails details() {
        return new OpportunityDetails("Renovacao do parque de TI", null, LocalDate.now().plusDays(60), 12, null);
    }

    private static Opportunity open() {
        return Opportunity.open(details(), COMPANY, null, OWNER, null, "ana");
    }

    @Test
    void opensInProspectingWithDefaultProbabilityAndHistory() {
        Opportunity opportunity = open();

        assertThat(opportunity.getStage()).isEqualTo(OpportunityStage.PROSPECTING);
        assertThat(opportunity.getProbability()).isEqualTo(10);
        assertThat(opportunity.getStageHistory()).hasSize(1);
        assertThat(opportunity.getStageHistory().get(0).changedBy()).isEqualTo("ana");
    }

    @Test
    void rejectsArchivedCompanyContactFromOtherCompanyAndInactiveOwner() {
        CompanyRef archived = new CompanyRef(11L, "Antiga", null, null, true);
        ContactRef foreign = new ContactRef(7L, 99L, "Fulano", null, true, false);
        SalesRepRef inactive = new SalesRepRef(2L, "Caio", null, null, false, false);

        assertThatThrownBy(() -> Opportunity.open(details(), archived, null, OWNER, null, "ana")).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> Opportunity.open(details(), COMPANY, foreign, OWNER, null, "ana")).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> Opportunity.open(details(), COMPANY, null, inactive, null, "ana")).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void computesOneTimeRecurringContractAndWeightedValues() {
        Opportunity opportunity = open();
        opportunity.addItem(NOTEBOOK, 2, new BigDecimal("5"));
        opportunity.addItem(M365, 50, BigDecimal.ZERO);

        assertThat(opportunity.oneTimeValue()).isEqualByComparingTo("16150.00");
        assertThat(opportunity.monthlyRecurringValue()).isEqualByComparingTo("3750.00");
        assertThat(opportunity.amount()).isEqualByComparingTo("61150.00");
        assertThat(opportunity.weightedAmount()).isEqualByComparingTo("6115.00");
        assertThat(opportunity.getDiscountApproval()).isEqualTo(DiscountApprovalStatus.NOT_REQUIRED);
    }

    @Test
    void usesEstimatedValueWhileThereAreNoItems() {
        OpportunityDetails withEstimate = new OpportunityDetails("Projeto", null, LocalDate.now().plusDays(10), 12,
                new BigDecimal("200000.00"));
        Opportunity opportunity = Opportunity.open(withEstimate, COMPANY, null, OWNER, 3L, "ana");

        assertThat(opportunity.amount()).isEqualByComparingTo("200000.00");
        assertThat(opportunity.getLeadId()).isEqualTo(3L);
    }

    @Test
    void discountAboveLimitRequiresManagerApprovalBeforeWinning() {
        Opportunity opportunity = open();
        opportunity.addItem(NOTEBOOK, 10, new BigDecimal("12"));
        assertThat(opportunity.getDiscountApproval()).isEqualTo(DiscountApprovalStatus.PENDING);
        assertThatThrownBy(() -> opportunity.markWon("ana")).isInstanceOf(BusinessRuleException.class);

        assertThatThrownBy(() -> opportunity.decideDiscount(OWNER, OWNER, true, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("gestor");
        assertThatThrownBy(() -> opportunity.decideDiscount(MANAGER, OWNER, false, " "))
                .isInstanceOf(IllegalArgumentException.class);

        opportunity.decideDiscount(MANAGER, OWNER, true, "cliente estrategico");
        assertThat(opportunity.getDiscountApproval()).isEqualTo(DiscountApprovalStatus.APPROVED);
        assertThat(opportunity.getApprovalDecidedBy()).isEqualTo(100L);

        opportunity.markWon("ana");
        assertThat(opportunity.getStage()).isEqualTo(OpportunityStage.WON);
        assertThat(opportunity.getProbability()).isEqualTo(100);
        assertThat(opportunity.getClosedAt()).isNotNull();
    }

    @Test
    void stageMovesResetProbabilityAndClosingRequiresDedicatedActions() {
        Opportunity opportunity = open();
        opportunity.moveTo(OpportunityStage.NEGOTIATION, "ana");
        assertThat(opportunity.getProbability()).isEqualTo(75);
        opportunity.adjustProbability(90);
        opportunity.moveTo(OpportunityStage.PROPOSAL, "ana");
        assertThat(opportunity.getProbability()).isEqualTo(50);

        assertThatThrownBy(() -> opportunity.moveTo(OpportunityStage.WON, "ana")).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> opportunity.adjustProbability(100)).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> opportunity.markWon("ana")).hasMessageContaining("ao menos um item");
    }

    @Test
    void lostOpportunityRequiresReasonAndReopensInThePreviousStage() {
        Opportunity opportunity = open();
        opportunity.moveTo(OpportunityStage.PROPOSAL, "ana");

        assertThatThrownBy(() -> opportunity.markLost(null, "ana")).isInstanceOf(IllegalArgumentException.class);
        opportunity.markLost("preco acima do concorrente", "ana");
        assertThat(opportunity.getProbability()).isZero();
        assertThatThrownBy(() -> opportunity.addItem(NOTEBOOK, 1, null)).isInstanceOf(BusinessRuleException.class);

        opportunity.reopen("ana");
        assertThat(opportunity.getStage()).isEqualTo(OpportunityStage.PROPOSAL);
        assertThat(opportunity.getLossReason()).isNull();
        assertThat(opportunity.getStageHistory()).hasSize(4);
    }

    @Test
    void unsellableProductCannotBeAdded() {
        ProductRef discontinued = new ProductRef(8L, "HW-SRV-000003", "Servidor antigo", "HARDWARE",
                BillingType.ONE_TIME, BigDecimal.TEN, BigDecimal.ZERO, false, false);

        assertThatThrownBy(() -> open().addItem(discontinued, 1, null)).isInstanceOf(BusinessRuleException.class);
    }
}
