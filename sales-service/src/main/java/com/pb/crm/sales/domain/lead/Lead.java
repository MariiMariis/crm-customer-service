package com.pb.crm.sales.domain.lead;

import com.pb.crm.commons.domain.AggregateRoot;
import com.pb.crm.commons.domain.AuditInfo;
import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.sales.domain.reference.SalesRepRef;

import java.time.Instant;

public class Lead extends AggregateRoot {

    public static final String CREATED = "lead.created";
    public static final String ASSIGNED = "lead.assigned";
    public static final String QUALIFIED = "lead.qualified";
    public static final String DISQUALIFIED = "lead.disqualified";
    public static final String CONVERSION_REQUESTED = "lead.conversion-requested";
    public static final String CONVERTED = "lead.converted";

    private LeadDetails details;
    private LeadStatus status;
    private int score;
    private Long ownerId;
    private String disqualifyReason;
    private ConversionRequest conversion;
    private String conversionFailureReason;
    private Long convertedCompanyId;
    private Long convertedContactId;
    private Long convertedOpportunityId;
    private Instant convertedAt;

    private Lead() {
    }

    public static Lead capture(LeadDetails details, SalesRepRef owner) {
        Lead lead = new Lead();
        lead.status = LeadStatus.NEW;
        lead.details = validate(details);
        lead.refreshScore();
        lead.recordEvent(CREATED);
        if (owner != null) {
            lead.assignTo(owner);
        }
        return lead;
    }

    public static Lead rehydrate(Long id,
                                 LeadDetails details,
                                 LeadStatus status,
                                 int score,
                                 Long ownerId,
                                 String disqualifyReason,
                                 ConversionRequest conversion,
                                 String conversionFailureReason,
                                 Long convertedCompanyId,
                                 Long convertedContactId,
                                 Long convertedOpportunityId,
                                 Instant convertedAt,
                                 boolean archived,
                                 Instant archivedAt,
                                 AuditInfo audit) {
        Lead lead = new Lead();
        lead.rehydrateBase(id, audit, archived, archivedAt);
        lead.details = details;
        lead.status = status;
        lead.score = score;
        lead.ownerId = ownerId;
        lead.disqualifyReason = disqualifyReason;
        lead.conversion = conversion;
        lead.conversionFailureReason = conversionFailureReason;
        lead.convertedCompanyId = convertedCompanyId;
        lead.convertedContactId = convertedContactId;
        lead.convertedOpportunityId = convertedOpportunityId;
        lead.convertedAt = convertedAt;
        return lead;
    }

    public void update(LeadDetails newDetails) {
        assertNotArchived();
        assertEditable("editar");
        this.details = validate(newDetails);
        refreshScore();
    }

    public void assignTo(SalesRepRef owner) {
        assertNotArchived();
        if (!status.isOpen()) {
            throw new BusinessRuleException("apenas leads abertos podem ser atribuidos");
        }
        if (owner == null) {
            throw new IllegalArgumentException("vendedor responsavel e obrigatorio");
        }
        if (!owner.canOwnRecords()) {
            throw new BusinessRuleException("o vendedor informado esta inativo ou arquivado");
        }
        if (owner.id().equals(ownerId)) {
            throw new BusinessRuleException("o lead ja esta atribuido a este vendedor");
        }
        this.ownerId = owner.id();
        recordEvent(ASSIGNED);
    }

    public void markContacted() {
        transition(LeadStatus.NEW, LeadStatus.CONTACTED, "marcar como contatado");
    }

    public void qualify() {
        assertHasOwner("qualificar");
        transition(LeadStatus.CONTACTED, LeadStatus.QUALIFIED, "qualificar");
        recordEvent(QUALIFIED);
    }

    public void disqualify(String reason) {
        assertNotArchived();
        assertEditable("desqualificar");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("o motivo da desqualificacao e obrigatorio");
        }
        this.status = LeadStatus.UNQUALIFIED;
        this.disqualifyReason = reason.trim();
        refreshScore();
        recordEvent(DISQUALIFIED);
    }

    public void reopen() {
        assertNotArchived();
        if (status != LeadStatus.UNQUALIFIED) {
            throw new BusinessRuleException("apenas leads desqualificados podem ser reabertos");
        }
        this.status = LeadStatus.NEW;
        this.disqualifyReason = null;
        refreshScore();
    }

    public void requestConversion(ConversionRequest request) {
        assertNotArchived();
        assertHasOwner("converter");
        if (status != LeadStatus.QUALIFIED) {
            throw new BusinessRuleException("apenas leads qualificados podem ser convertidos");
        }
        if (request == null) {
            throw new IllegalArgumentException("dados da conversao sao obrigatorios");
        }
        if (details.email() == null) {
            throw new BusinessRuleException("informe o e-mail do lead antes de converter; ele sera o contato da empresa");
        }
        this.status = LeadStatus.CONVERTING;
        this.conversion = request;
        this.conversionFailureReason = null;
        refreshScore();
        recordEvent(CONVERSION_REQUESTED);
    }

    public void completeConversion(Long companyId, Long contactId, Long opportunityId) {
        if (status != LeadStatus.CONVERTING) {
            throw new BusinessRuleException("o lead nao esta em conversao");
        }
        if (companyId == null || contactId == null) {
            throw new IllegalArgumentException("empresa e contato sao obrigatorios para concluir a conversao");
        }
        this.status = LeadStatus.CONVERTED;
        this.convertedCompanyId = companyId;
        this.convertedContactId = contactId;
        this.convertedOpportunityId = opportunityId;
        this.convertedAt = Instant.now();
        refreshScore();
        recordEvent(CONVERTED);
    }

    public void failConversion(String reason) {
        if (status != LeadStatus.CONVERTING) {
            throw new BusinessRuleException("o lead nao esta em conversao");
        }
        this.status = LeadStatus.QUALIFIED;
        this.conversionFailureReason = reason == null || reason.isBlank() ? "falha nao informada" : reason.trim();
        refreshScore();
    }

    @Override
    public void archive() {
        if (status == LeadStatus.CONVERTING) {
            throw new BusinessRuleException("nao e possivel arquivar um lead em conversao");
        }
        super.archive();
    }

    public LeadScore scoreBreakdown() {
        return LeadScore.evaluate(details, status);
    }

    public String fullName() {
        return details.firstName() + " " + details.lastName();
    }

    private void transition(LeadStatus expected, LeadStatus target, String action) {
        assertNotArchived();
        if (status != expected) {
            throw new BusinessRuleException("nao e possivel %s um lead com status %s (esperado %s)"
                    .formatted(action, status, expected));
        }
        this.status = target;
        refreshScore();
    }

    private void assertEditable(String action) {
        if (!status.isEditable()) {
            throw new BusinessRuleException("nao e possivel %s um lead com status %s".formatted(action, status));
        }
    }

    private void assertHasOwner(String action) {
        if (ownerId == null) {
            throw new BusinessRuleException("atribua um vendedor responsavel antes de %s o lead".formatted(action));
        }
    }

    private void refreshScore() {
        this.score = LeadScore.evaluate(details, status).total();
    }

    private static LeadDetails validate(LeadDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("dados do lead sao obrigatorios");
        }
        if (details.source() == null) {
            throw new IllegalArgumentException("origem e obrigatoria");
        }
        String email = blankToNull(details.email());
        String phone = blankToNull(details.phone());
        if (email == null && phone == null) {
            throw new BusinessRuleException("informe ao menos um e-mail ou telefone para o lead");
        }
        if (details.estimatedValue() != null && details.estimatedValue().signum() < 0) {
            throw new BusinessRuleException("o valor estimado nao pode ser negativo");
        }
        return new LeadDetails(
                requireText(details.firstName(), "nome"),
                requireText(details.lastName(), "sobrenome"),
                email == null ? null : email.toLowerCase(),
                phone,
                requireText(details.companyName(), "empresa"),
                blankToNull(details.jobTitle()),
                details.source(),
                details.estimatedValue(),
                blankToNull(details.notes())
        );
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " e obrigatorio");
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public LeadDetails getDetails() {
        return details;
    }

    public LeadStatus getStatus() {
        return status;
    }

    public int getScore() {
        return score;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getDisqualifyReason() {
        return disqualifyReason;
    }

    public ConversionRequest getConversion() {
        return conversion;
    }

    public String getConversionFailureReason() {
        return conversionFailureReason;
    }

    public Long getConvertedCompanyId() {
        return convertedCompanyId;
    }

    public Long getConvertedContactId() {
        return convertedContactId;
    }

    public Long getConvertedOpportunityId() {
        return convertedOpportunityId;
    }

    public Instant getConvertedAt() {
        return convertedAt;
    }
}
