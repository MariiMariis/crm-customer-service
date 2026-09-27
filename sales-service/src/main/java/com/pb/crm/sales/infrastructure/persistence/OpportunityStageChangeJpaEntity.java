package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.sales.domain.opportunity.OpportunityStage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "opportunity_stage_history")
public class OpportunityStageChangeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opportunity_stage_history_seq")
    @SequenceGenerator(name = "opportunity_stage_history_seq", sequenceName = "opportunity_stage_history_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private OpportunityJpaEntity opportunity;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_stage", length = 20)
    private OpportunityStage fromStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_stage", nullable = false, length = 20)
    private OpportunityStage toStage;

    @Column(nullable = false)
    private int probability;

    @Column(length = 500)
    private String reason;

    @Column(name = "changed_by", length = 120)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    protected OpportunityStageChangeJpaEntity() {
    }

    public OpportunityStageChangeJpaEntity(OpportunityJpaEntity opportunity,
                                           OpportunityStage fromStage,
                                           OpportunityStage toStage,
                                           int probability,
                                           String reason,
                                           String changedBy,
                                           Instant changedAt) {
        this.opportunity = opportunity;
        this.fromStage = fromStage;
        this.toStage = toStage;
        this.probability = probability;
        this.reason = reason;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }

    public Long getId() {
        return id;
    }

    public OpportunityStage getFromStage() {
        return fromStage;
    }

    public OpportunityStage getToStage() {
        return toStage;
    }

    public int getProbability() {
        return probability;
    }

    public String getReason() {
        return reason;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
