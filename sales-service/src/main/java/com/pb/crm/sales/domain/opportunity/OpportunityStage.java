package com.pb.crm.sales.domain.opportunity;

public enum OpportunityStage {
    PROSPECTING(10),
    QUALIFICATION(25),
    PROPOSAL(50),
    NEGOTIATION(75),
    WON(100),
    LOST(0);

    private final int defaultProbability;

    OpportunityStage(int defaultProbability) {
        this.defaultProbability = defaultProbability;
    }

    public int defaultProbability() {
        return defaultProbability;
    }

    public boolean isOpen() {
        return this != WON && this != LOST;
    }
}
