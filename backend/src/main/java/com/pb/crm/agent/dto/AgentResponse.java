package com.pb.crm.agent.dto;

import com.pb.crm.agent.Agent;

import java.time.Instant;

public record AgentResponse(
        Long id,
        String name,
        String email,
        String department,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy,
        Long version
) {
    public static AgentResponse fromEntity(Agent agent) {
        return new AgentResponse(
                agent.getId(),
                agent.getName(),
                agent.getEmail(),
                agent.getDepartment(),
                agent.isActive(),
                agent.getCreatedAt(),
                agent.getUpdatedAt(),
                agent.getCreatedBy(),
                agent.getUpdatedBy(),
                agent.getVersion()
        );
    }
}
