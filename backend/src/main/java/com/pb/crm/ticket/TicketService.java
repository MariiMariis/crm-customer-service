package com.pb.crm.ticket;

import com.pb.crm.ticket.dto.InteractionRequest;
import com.pb.crm.ticket.dto.InteractionResponse;
import com.pb.crm.ticket.dto.TicketRequest;
import com.pb.crm.ticket.dto.TicketResponse;

import java.util.List;

public interface TicketService {

    TicketResponse create(TicketRequest request);

    TicketResponse update(Long id, TicketRequest request);

    TicketResponse changeStatus(Long id, TicketStatus status);

    TicketResponse findById(Long id);

    List<TicketResponse> findAll();

    List<TicketResponse> findByStatus(TicketStatus status);

    InteractionResponse addInteraction(Long ticketId, InteractionRequest request);

    void delete(Long id);
}
