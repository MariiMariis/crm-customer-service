package com.pb.crm.accounts.domain.contact;

public record ContactProfile(
        String firstName,
        String lastName,
        String email,
        String phone,
        String mobile,
        String jobTitle,
        String department,
        DecisionRole decisionRole
) {
}
