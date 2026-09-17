package com.pb.crm.agent.dto;

import com.pb.crm.agent.Agent;

public record AgentResponse(
        Long id,
        String name,
        String email,
        String department,
        boolean active
) {
    public static AgentResponse fromEntity(Agent agent) {
        return new AgentResponse(agent.getId(), agent.getName(), agent.getEmail(), agent.getDepartment(), agent.isActive());
    }
}
