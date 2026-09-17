package com.pb.crm.agent;

import com.pb.crm.agent.dto.AgentRequest;
import com.pb.crm.agent.dto.AgentResponse;
import com.pb.crm.audit.RevisionResponse;
import com.pb.crm.common.BusinessRuleException;
import com.pb.crm.common.ResourceNotFoundException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
        if (agentRepository.existsByEmailIgnoreCase(request.email().trim())) {
            throw new BusinessRuleException("ja existe um atendente cadastrado com este email");
        }
        Agent agent = new Agent(request.name(), request.email(), request.department());
        return AgentResponse.fromEntity(agentRepository.save(agent));
    }

    @Override
    @Transactional
    public AgentResponse update(Long id, AgentRequest request) {
        Agent agent = findEntityById(id);
        if (request.version() != null && !request.version().equals(agent.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Agent.class, id);
        }
        if (agentRepository.existsByEmailIgnoreCaseAndIdNot(request.email().trim(), id)) {
            throw new BusinessRuleException("ja existe outro atendente cadastrado com este email");
        }
        agent.update(request.name(), request.email(), request.department(), request.active());
        return AgentResponse.fromEntity(agentRepository.saveAndFlush(agent));
    }

    @Override
    @Transactional(readOnly = true)
    public AgentResponse findById(Long id) {
        return AgentResponse.fromEntity(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentResponse> findAll() {
        return agentRepository.findAllByOrderByNameAsc().stream()
                .map(AgentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentResponse> findActive() {
        return agentRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(AgentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        agentRepository.delete(findEntityById(id));
        agentRepository.flush();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevisionResponse<AgentResponse>> findRevisions(Long id) {
        return agentRepository.findRevisions(id).stream()
                .map(revision -> RevisionResponse.from(revision, AgentResponse::fromEntity))
                .toList();
    }

    private Agent findEntityById(Long id) {
        return agentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Atendente", id));
    }
}
