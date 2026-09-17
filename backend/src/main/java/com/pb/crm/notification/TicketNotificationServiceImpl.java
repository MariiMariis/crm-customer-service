package com.pb.crm.notification;

import com.pb.crm.common.ResourceNotFoundException;
import com.pb.crm.config.RequestActor;
import com.pb.crm.customer.CustomerRepository;
import com.pb.crm.notification.dto.ManualNotificationRequest;
import com.pb.crm.notification.dto.NotificationPreferenceRequest;
import com.pb.crm.notification.dto.NotificationPreferenceResponse;
import com.pb.crm.notification.dto.NotificationRequest;
import com.pb.crm.notification.dto.NotificationResponse;
import com.pb.crm.notification.dto.NotificationServiceStatus;
import com.pb.crm.ticket.TicketRepository;
import com.pb.crm.ticket.event.TicketSnapshot;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketNotificationServiceImpl implements TicketNotificationService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final NotificationGateway gateway;
    private final NotificationComposer composer;

    public TicketNotificationServiceImpl(TicketRepository ticketRepository,
                                         CustomerRepository customerRepository,
                                         NotificationGateway gateway,
                                         NotificationComposer composer) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.gateway = gateway;
        this.composer = composer;
    }

    @Override
    public List<NotificationResponse> findByTicket(Long ticketId) {
        assertTicketExists(ticketId);
        return gateway.findByTicket(ticketId);
    }

    @Override
    public NotificationResponse sendManual(Long ticketId, ManualNotificationRequest request) {
        TicketSnapshot snapshot = loadSnapshot(ticketId);
        NotificationRequest notification = composer.manual(snapshot, request, RequestActor.current());
        return gateway.send(notification)
                .orElseThrow(() -> new NotificationServiceUnavailableException(
                        "nao foi possivel registrar a notificacao: o servico de notificacoes esta indisponivel"));
    }

    @Override
    public NotificationPreferenceResponse findPreferences(Long customerId) {
        assertCustomerExists(customerId);
        return gateway.findPreferences(customerId);
    }

    @Override
    public NotificationPreferenceResponse updatePreferences(Long customerId, NotificationPreferenceRequest request) {
        assertCustomerExists(customerId);
        return gateway.updatePreferences(customerId, request);
    }

    @Override
    public NotificationServiceStatus status() {
        return gateway.status();
    }

    private TicketSnapshot loadSnapshot(Long ticketId) {
        return ticketRepository.findWithDetailsById(ticketId)
                .map(TicketSnapshot::from)
                .orElseThrow(() -> ResourceNotFoundException.forId("Ticket", ticketId));
    }

    private void assertTicketExists(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw ResourceNotFoundException.forId("Ticket", ticketId);
        }
    }

    private void assertCustomerExists(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw ResourceNotFoundException.forId("Cliente", customerId);
        }
    }
}
