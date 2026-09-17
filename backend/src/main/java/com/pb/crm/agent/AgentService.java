package com.pb.crm.agent;

import com.pb.crm.agent.dto.AgentRequest;
import com.pb.crm.agent.dto.AgentResponse;
import com.pb.crm.audit.RevisionResponse;

import java.util.List;

public interface AgentService {

    AgentResponse create(AgentRequest request);

    AgentResponse update(Long id, AgentRequest request);

    AgentResponse findById(Long id);

    List<AgentResponse> findAll();

    List<AgentResponse> findActive();

    void delete(Long id);

    List<RevisionResponse<AgentResponse>> findRevisions(Long id);
}
