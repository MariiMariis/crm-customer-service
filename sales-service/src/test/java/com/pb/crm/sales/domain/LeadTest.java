package com.pb.crm.sales.domain;

import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.sales.domain.lead.ConversionRequest;
import com.pb.crm.sales.domain.lead.Lead;
import com.pb.crm.sales.domain.lead.LeadDetails;
import com.pb.crm.sales.domain.lead.LeadScore;
import com.pb.crm.sales.domain.lead.LeadSource;
import com.pb.crm.sales.domain.lead.LeadStatus;
import com.pb.crm.sales.domain.lead.ScoreFactor;
import com.pb.crm.sales.domain.reference.SalesRepRef;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeadTest {

    private static final SalesRepRef ANA = new SalesRepRef(1L, "Ana", "ana@pbtech.com.br", null, true, false);

    private static LeadDetails details(String jobTitle, LeadSource source, String value) {
        return new LeadDetails("Rodrigo", "Nunes", "Rodrigo.Nunes@LogiSul.com.br", "51999990000", "LogiSul Transportes",
                jobTitle, source, value == null ? null : new BigDecimal(value), null);
    }

    private static ConversionRequest conversion() {
        return new ConversionRequest("11.222.333/0001-81", "logistics", "medium", "Porto Alegre", "rs", true,
                "Renovacao do parque de notebooks", LocalDate.now().plusDays(30), null);
    }

    @Test
    void scoreCombinesContactTitleSourceValueAndStage() {
        LeadScore score = LeadScore.evaluate(details("Diretora de Tecnologia", LeadSource.REFERRAL, "650000"), LeadStatus.QUALIFIED);

        assertThat(score.total()).isEqualTo(100);
        assertThat(score.factors()).extracting(ScoreFactor::points).contains(10, 10, 20, 20, 25, 15);

        LeadScore modest = LeadScore.evaluate(details("Analista de TI", LeadSource.COLD_CALL, "5000"), LeadStatus.NEW);
        assertThat(modest.total()).isEqualTo(20);

        LeadScore manager = LeadScore.evaluate(details("Gerente de Infraestrutura", LeadSource.LINKEDIN, "120000"), LeadStatus.CONTACTED);
        assertThat(manager.total()).isEqualTo(10 + 10 + 10 + 5 + 15 + 5);
    }

    @Test
    void leadRequiresEmailOrPhoneAndNormalizesEmail() {
        LeadDetails noContact = new LeadDetails("A", "B", " ", null, "Empresa", null, LeadSource.WEBSITE, null, null);

        assertThatThrownBy(() -> Lead.capture(noContact, null)).isInstanceOf(BusinessRuleException.class);
        assertThat(Lead.capture(details(null, LeadSource.WEBSITE, null), null).getDetails().email())
                .isEqualTo("rodrigo.nunes@logisul.com.br");
    }

    @Test
    void funnelRequiresOrderAndOwnerToQualify() {
        Lead lead = Lead.capture(details("CTO", LeadSource.EVENT, "90000"), null);

        assertThatThrownBy(lead::qualify).isInstanceOf(BusinessRuleException.class).hasMessageContaining("vendedor");
        lead.assignTo(ANA);
        assertThatThrownBy(lead::qualify).isInstanceOf(BusinessRuleException.class).hasMessageContaining("esperado CONTACTED");

        int before = lead.getScore();
        lead.markContacted();
        lead.qualify();
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.QUALIFIED);
        assertThat(lead.getScore()).isGreaterThan(before);
    }

    @Test
    void disqualifyRequiresReasonAndCanBeReopened() {
        Lead lead = Lead.capture(details(null, LeadSource.CAMPAIGN, null), ANA);

        assertThatThrownBy(() -> lead.disqualify(" ")).isInstanceOf(IllegalArgumentException.class);
        lead.disqualify("sem orcamento para 2026");
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.UNQUALIFIED);
        assertThatThrownBy(() -> lead.update(details(null, LeadSource.CAMPAIGN, null))).isInstanceOf(BusinessRuleException.class);

        lead.reopen();
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.NEW);
        assertThat(lead.getDisqualifyReason()).isNull();
    }

    @Test
    void conversionSagaStatesCompleteOrFallBackToQualified() {
        Lead lead = Lead.capture(details("CIO", LeadSource.PARTNER, "300000"), ANA);
        assertThatThrownBy(() -> lead.requestConversion(conversion())).isInstanceOf(BusinessRuleException.class);

        lead.markContacted();
        lead.qualify();
        lead.requestConversion(conversion());
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.CONVERTING);
        assertThat(lead.getConversion().industry()).isEqualTo("LOGISTICS");
        assertThatThrownBy(lead::archive).isInstanceOf(BusinessRuleException.class);

        lead.failConversion("CNPJ invalido");
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.QUALIFIED);
        assertThat(lead.getConversionFailureReason()).isEqualTo("CNPJ invalido");

        lead.requestConversion(conversion());
        lead.completeConversion(10L, 20L, 30L);
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.CONVERTED);
        assertThat(lead.getConvertedAt()).isNotNull();
        assertThatThrownBy(() -> lead.assignTo(new SalesRepRef(2L, "Bia", null, null, true, false)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void conversionRequestValidatesMandatoryFields() {
        assertThatThrownBy(() -> new ConversionRequest(null, "RETAIL", "SMALL", null, null, false, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConversionRequest("11222333000181", "RETAIL", "SMALL", null, null, true, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
