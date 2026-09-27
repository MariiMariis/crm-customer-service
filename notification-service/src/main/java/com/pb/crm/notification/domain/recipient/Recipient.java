package com.pb.crm.notification.domain.recipient;

public record Recipient(
        Long id,
        String name,
        String email,
        Long managerId,
        boolean active,
        boolean archived
) {
    public boolean canReceive() {
        return !archived;
    }

    public boolean hasEmail() {
        return email != null && !email.isBlank();
    }
}
