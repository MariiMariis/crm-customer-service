package com.pb.crm.ticket;

import com.pb.crm.agent.Agent;
import com.pb.crm.agent.AgentRepository;
import com.pb.crm.audit.RevisionResponse;
import com.pb.crm.common.BusinessRuleException;
import com.pb.crm.common.PageResponse;
import com.pb.crm.common.ResourceNotFoundException;
import com.pb.crm.customer.Customer;
import com.pb.crm.customer.CustomerRepository;
import com.pb.crm.ticket.dto.InteractionRequest;
import com.pb.crm.ticket.dto.InteractionResponse;
import com.pb.crm.ticket.dto.TicketRequest;
import com.pb.crm.ticket.dto.TicketResponse;
import com.pb.crm.ticket.dto.TicketStatsResponse;
import com.pb.crm.ticket.dto.TicketStatusHistoryResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketStatusHistoryRepository statusHistoryRepository;
    private final CustomerRepository customerRepository;
    private final AgentRepository agentRepository;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             TicketStatusHistoryRepository statusHistoryRepository,
                             CustomerRepository customerRepository,
                             AgentRepository agentRepository) {
        this.ticketRepository = ticketRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.customerRepository = customerRepository;
        this.agentRepository = agentRepository;
    }

    @Override
    @Transactional
    public TicketResponse create(TicketRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Cliente", request.customerId()));
        Agent agent = resolveAgent(request.agentId());
        Ticket ticket = new Ticket(request.subject(), request.description(), request.priority(), customer, agent);
        return TicketResponse.fromEntity(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketResponse update(Long id, TicketRequest request) {
        Ticket ticket = findEntityById(id);
        Agent agent = resolveAgent(request.agentId());
        ticket.update(request.subject(), request.description(), request.priority(), agent);
        return TicketResponse.fromEntity(ticket);
    }

    @Override
    @Transactional
    public TicketResponse changeStatus(Long id, TicketStatus status, String reason) {
        Ticket ticket = findEntityById(id);
        ticket.changeStatus(status, reason);
        return TicketResponse.fromEntity(ticketRepository.saveAndFlush(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse findById(Long id) {
        return ticketRepository.findWithDetailsById(id)
                .map(TicketResponse::fromEntity)
                .orElseThrow(() -> ResourceNotFoundException.forId("Ticket", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findAll() {
        return toSummaries(ticketRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findByStatus(TicketStatus status) {
        return toSummaries(ticketRepository.findByStatusOrderByCreatedAtDesc(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findByCustomer(Long customerId) {
        return toSummaries(ticketRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findByStatusAndCustomer(TicketStatus status, Long customerId) {
        return toSummaries(ticketRepository.findByStatusAndCustomer_IdOrderByCreatedAtDesc(status, customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> search(TicketFilter filter, Pageable pageable) {
        return PageResponse.from(
                ticketRepository.findAll(TicketSpecifications.withFilter(filter), pageable),
                TicketResponse::summaryFromEntity
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TicketStatsResponse stats() {
        Map<TicketStatus, Long> byStatus = new EnumMap<>(TicketStatus.class);
        for (TicketStatus status : TicketStatus.values()) {
            byStatus.put(status, 0L);
        }
        long total = 0;
        for (TicketStatusCount count : ticketRepository.countGroupedByStatus()) {
            byStatus.put(count.getStatus(), count.getTotal());
            total += count.getTotal();
        }
        return new TicketStatsResponse(total, byStatus);
    }

    @Override
    @Transactional
    public InteractionResponse addInteraction(Long ticketId, InteractionRequest request) {
        Ticket ticket = findEntityById(ticketId);
        Interaction interaction = ticket.addInteraction(request.author(), request.message());
        ticketRepository.saveAndFlush(ticket);
        return InteractionResponse.fromEntity(interaction);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Ticket ticket = findEntityById(id);
        if (ticket.getStatus() == TicketStatus.IN_PROGRESS) {
            throw new BusinessRuleException("nao e possivel excluir um ticket em andamento");
        }
        ticketRepository.delete(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketStatusHistoryResponse> findStatusHistory(Long ticketId) {
        assertTicketExists(ticketId);
        return statusHistoryRepository.findByTicket_IdOrderByChangedAtAscIdAsc(ticketId).stream()
                .map(TicketStatusHistoryResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevisionResponse<TicketResponse>> findRevisions(Long ticketId) {
        return ticketRepository.findRevisions(ticketId).stream()
                .map(revision -> RevisionResponse.from(revision, TicketResponse::summaryFromEntity))
                .toList();
    }

    private Ticket findEntityById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Ticket", id));
    }

    private void assertTicketExists(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Ticket", id);
        }
    }

    private Agent resolveAgent(Long agentId) {
        if (agentId == null) {
            return null;
        }
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Atendente", agentId));
        if (!agent.isActive()) {
            throw new BusinessRuleException("o atendente informado esta inativo e nao pode receber tickets");
        }
        return agent;
    }

    private static List<TicketResponse> toSummaries(List<Ticket> tickets) {
        return tickets.stream().map(TicketResponse::summaryFromEntity).toList();
    }
}
