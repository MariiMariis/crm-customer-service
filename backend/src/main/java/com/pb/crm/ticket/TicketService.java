package com.pb.crm.ticket;

import com.pb.crm.audit.RevisionResponse;
import com.pb.crm.common.PageResponse;
import com.pb.crm.ticket.dto.InteractionRequest;
import com.pb.crm.ticket.dto.InteractionResponse;
import com.pb.crm.ticket.dto.TicketRequest;
import com.pb.crm.ticket.dto.TicketResponse;
import com.pb.crm.ticket.dto.TicketStatsResponse;
import com.pb.crm.ticket.dto.TicketStatusHistoryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketService {

    TicketResponse create(TicketRequest request);

    TicketResponse update(Long id, TicketRequest request);

    TicketResponse changeStatus(Long id, TicketStatus status, String reason);

    TicketResponse findById(Long id);

    List<TicketResponse> findAll();

    List<TicketResponse> findByStatus(TicketStatus status);

    List<TicketResponse> findByCustomer(Long customerId);

    List<TicketResponse> findByStatusAndCustomer(TicketStatus status, Long customerId);

    PageResponse<TicketResponse> search(TicketFilter filter, Pageable pageable);

    TicketStatsResponse stats();

    InteractionResponse addInteraction(Long ticketId, InteractionRequest request);

    void delete(Long id);

    List<TicketStatusHistoryResponse> findStatusHistory(Long ticketId);

    List<RevisionResponse<TicketResponse>> findRevisions(Long ticketId);
}
