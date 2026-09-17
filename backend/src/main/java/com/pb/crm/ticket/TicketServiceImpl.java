package com.pb.crm.ticket;

import com.pb.crm.agent.AgentRepository;
import com.pb.crm.common.ResourceNotFoundException;
import com.pb.crm.customer.CustomerRepository;
import com.pb.crm.ticket.dto.InteractionRequest;
import com.pb.crm.ticket.dto.InteractionResponse;
import com.pb.crm.ticket.dto.TicketRequest;
import com.pb.crm.ticket.dto.TicketResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final AgentRepository agentRepository;

    public TicketServiceImpl(TicketRepository ticketRepository,
                              CustomerRepository customerRepository,
                              AgentRepository agentRepository) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.agentRepository = agentRepository;
    }

    @Override
    @Transactional
    public TicketResponse create(TicketRequest request) {
        assertCustomerExists(request.customerId());
        assertAgentExistsIfPresent(request.agentId());

        Ticket ticket = new Ticket(
                request.subject(),
                request.description(),
                request.priority(),
                request.customerId(),
                request.agentId()
        );
        return TicketResponse.fromEntity(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketResponse update(Long id, TicketRequest request) {
        assertAgentExistsIfPresent(request.agentId());
        Ticket ticket = findEntityById(id);
        ticket.update(request.subject(), request.description(), request.priority(), request.agentId());
        return TicketResponse.fromEntity(ticket);
    }

    @Override
    @Transactional
    public TicketResponse changeStatus(Long id, TicketStatus status) {
        Ticket ticket = findEntityById(id);
        ticket.changeStatus(status);
        return TicketResponse.fromEntity(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse findById(Long id) {
        return TicketResponse.fromEntity(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findAll() {
        return ticketRepository.findAll().stream()
                .map(TicketResponse::summaryFromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status).stream()
                .map(TicketResponse::summaryFromEntity)
                .toList();
    }

    @Override
    @Transactional
    public InteractionResponse addInteraction(Long ticketId, InteractionRequest request) {
        Ticket ticket = findEntityById(ticketId);
        Interaction interaction = ticket.addInteraction(request.author(), request.message());
        ticketRepository.save(ticket);
        return InteractionResponse.fromEntity(interaction);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ticketRepository.delete(findEntityById(id));
    }

    private Ticket findEntityById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Ticket", id));
    }

    private void assertCustomerExists(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw ResourceNotFoundException.forId("Cliente", customerId);
        }
    }

    private void assertAgentExistsIfPresent(Long agentId) {
        if (agentId != null && !agentRepository.existsById(agentId)) {
            throw ResourceNotFoundException.forId("Atendente", agentId);
        }
    }
}
