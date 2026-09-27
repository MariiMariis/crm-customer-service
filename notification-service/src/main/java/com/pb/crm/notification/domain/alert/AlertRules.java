package com.pb.crm.notification.domain.alert;

import com.pb.crm.notification.domain.notification.NotificationType;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class AlertRules {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    public record LeadFacts(Long leadId, String leadName, String companyName, int score, String ownerName) {
    }

    public record OpportunityFacts(Long opportunityId,
                                   String title,
                                   String companyName,
                                   String ownerName,
                                   BigDecimal amount,
                                   String lossReason) {
    }

    private AlertRules() {
    }

    public static Alert leadAssigned(LeadFacts lead) {
        return new Alert(
                NotificationType.LEAD_ASSIGNED,
                List.of(RecipientRole.OWNER),
                "Novo lead atribuído: %s".formatted(lead.leadName()),
                "%s (%s) foi atribuído a você com score %d. Faça o primeiro contato o quanto antes."
                        .formatted(lead.leadName(), orDash(lead.companyName()), lead.score()),
                "/leads/" + lead.leadId()
        );
    }

    public static Alert discountApprovalRequested(OpportunityFacts opportunity) {
        return new Alert(
                NotificationType.DISCOUNT_APPROVAL_REQUESTED,
                List.of(RecipientRole.OWNER_MANAGER),
                "Aprovação de desconto pendente: %s".formatted(opportunity.title()),
                "%s solicitou desconto acima do limite do catálogo na oportunidade \"%s\" (%s), valor atual de %s."
                        .formatted(orDash(opportunity.ownerName()), opportunity.title(), orDash(opportunity.companyName()),
                                money(opportunity.amount())),
                "/opportunities/" + opportunity.opportunityId()
        );
    }

    public static Alert opportunityWon(OpportunityFacts opportunity) {
        return new Alert(
                NotificationType.OPPORTUNITY_WON,
                List.of(RecipientRole.OWNER, RecipientRole.OWNER_MANAGER),
                "Oportunidade ganha: %s".formatted(opportunity.title()),
                "%s fechou \"%s\" com %s no valor de %s."
                        .formatted(orDash(opportunity.ownerName()), opportunity.title(), orDash(opportunity.companyName()),
                                money(opportunity.amount())),
                "/opportunities/" + opportunity.opportunityId()
        );
    }

    public static Alert opportunityLost(OpportunityFacts opportunity) {
        return new Alert(
                NotificationType.OPPORTUNITY_LOST,
                List.of(RecipientRole.OWNER, RecipientRole.OWNER_MANAGER),
                "Oportunidade perdida: %s".formatted(opportunity.title()),
                "\"%s\" com %s (%s) foi perdida. Motivo: %s."
                        .formatted(opportunity.title(), orDash(opportunity.companyName()), money(opportunity.amount()),
                                orDash(opportunity.lossReason())),
                "/opportunities/" + opportunity.opportunityId()
        );
    }

    static String money(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(value == null ? BigDecimal.ZERO : value)
                .replace(' ', ' ');
    }

    private static String orDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
