package com.pb.crm.sales.domain.lead;

public enum LeadSource {
    WEBSITE(10),
    REFERRAL(20),
    EVENT(10),
    COLD_CALL(0),
    LINKEDIN(5),
    PARTNER(15),
    CAMPAIGN(5);

    private final int scorePoints;

    LeadSource(int scorePoints) {
        this.scorePoints = scorePoints;
    }

    public int scorePoints() {
        return scorePoints;
    }
}
