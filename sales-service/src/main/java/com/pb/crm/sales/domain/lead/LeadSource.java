package com.pb.crm.sales.domain.lead;

public enum LeadSource {
    WEBSITE(10, "site"),
    REFERRAL(20, "indicação"),
    EVENT(10, "evento"),
    COLD_CALL(0, "prospecção ativa"),
    LINKEDIN(5, "LinkedIn"),
    PARTNER(15, "parceiro"),
    CAMPAIGN(5, "campanha");

    private final int scorePoints;
    private final String label;

    LeadSource(int scorePoints, String label) {
        this.scorePoints = scorePoints;
        this.label = label;
    }

    public int scorePoints() {
        return scorePoints;
    }

    public String label() {
        return label;
    }
}
