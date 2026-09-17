package com.pb.crm.agent;

import com.pb.crm.agent.dto.AgentRequest;
import com.pb.crm.agent.dto.AgentResponse;
import com.pb.crm.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AgentServiceImpl implements AgentService {

    private final AgentRepository agentRepository;

    public AgentServiceImpl(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @Override
    @Transactional
    public AgentResponse create(AgentRequest request) {
        if (agentRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("ja existe um atendente cadastrado com este email");
        }
        Agent agent = new Agent(request.name(), request.email(), request.department());
        return AgentResponse.fromEntity(agentRepository.save(agent));
    }

    @Override
    @Transactional
    public AgentResponse update(Long id, AgentRequest request) {
        Agent agent = findEntityById(id);
        agent.update(request.name(), request.email(), request.department(), request.active());
        return AgentResponse.fromEntity(agent);
    }

    @Override
    @Transactional(readOnly = true)
    public AgentResponse findById(Long id) {
        return AgentResponse.fromEntity(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentResponse> findAll() {
        return agentRepository.findAll().stream()
                .map(AgentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        agentRepository.delete(findEntityById(id));
    }

    private Agent findEntityById(Long id) {
        return agentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Atendente", id));
    }
}
