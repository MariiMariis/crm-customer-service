package com.pb.crm.notification;

import com.pb.crm.agent.Agent;
import com.pb.crm.agent.AgentRepository;
import com.pb.crm.common.BusinessRuleException;
import com.pb.crm.customer.Customer;
import com.pb.crm.customer.CustomerRepository;
import com.pb.crm.notification.dto.NotificationRequest;
import com.pb.crm.ticket.TicketPriority;
import com.pb.crm.ticket.TicketService;
import com.pb.crm.ticket.TicketStatus;
import com.pb.crm.ticket.dto.InteractionRequest;
import com.pb.crm.ticket.dto.TicketRequest;
import com.pb.crm.ticket.dto.TicketResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:crmtest-notification;DB_CLOSE_DELAY=-1")
class TicketNotificationListenerTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AgentRepository agentRepository;

    @MockBean
    private NotificationGateway gateway;

    private Customer customer;
    private Agent agent;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        customer = customerRepository.save(new Customer("Cliente " + suffix, "cliente." + suffix + "@example.com", "11999990000", null));
        agent = agentRepository.save(new Agent("Agente " + suffix, "agente." + suffix + "@pbcrm.com", "Suporte"));
        when(gateway.send(any())).thenReturn(Optional.empty());
        clearInvocations(gateway);
    }

    @Test
    void devePublicarNotificacaoDeTicketCriadoAposCommit() {
        TicketResponse ticket = ticketService.create(new TicketRequest(
                "Sistema fora do ar", "Nao consigo acessar", TicketPriority.HIGH, customer.getId(), agent.getId()));

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(gateway, times(1)).send(captor.capture());
        NotificationRequest request = captor.getValue();
        assertThat(request.type()).isEqualTo(NotificationType.TICKET_CREATED);
        assertThat(request.channel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(request.ticketId()).isEqualTo(ticket.id());
        assertThat(request.customerId()).isEqualTo(customer.getId());
        assertThat(request.recipientEmail()).isEqualTo(customer.getEmail());
        assertThat(request.recipientName()).isEqualTo(customer.getName());
        assertThat(request.requestedBy()).isEqualTo("system");
    }

    @Test
    void devePublicarNotificacoesDeStatusEInteracao() {
        TicketResponse ticket = ticketService.create(new TicketRequest(
                "Duvida", "Como emitir fatura?", TicketPriority.LOW, customer.getId(), null));
        clearInvocations(gateway);

        ticketService.changeStatus(ticket.id(), TicketStatus.IN_PROGRESS, "analise iniciada");
        ticketService.addInteraction(ticket.id(), new InteractionRequest("Agente", "Estamos verificando."));

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(gateway, times(2)).send(captor.capture());
        List<NotificationRequest> requests = captor.getAllValues();

        assertThat(requests.get(0).type()).isEqualTo(NotificationType.TICKET_STATUS_CHANGED);
        assertThat(requests.get(0).message()).contains("de OPEN para IN_PROGRESS").contains("analise iniciada");
        assertThat(requests.get(1).type()).isEqualTo(NotificationType.TICKET_INTERACTION_ADDED);
        assertThat(requests.get(1).channel()).isEqualTo(NotificationChannel.IN_APP);
        assertThat(requests.get(1).message()).contains("Agente escreveu").contains("Estamos verificando.");
    }

    @Test
    void naoDeveNotificarQuandoATransacaoFalha() {
        TicketResponse ticket = ticketService.create(new TicketRequest(
                "Cancelamento", "Quero cancelar", TicketPriority.MEDIUM, customer.getId(), null));
        clearInvocations(gateway);

        assertThatThrownBy(() -> ticketService.changeStatus(ticket.id(), TicketStatus.OPEN, "repetido"))
                .isInstanceOf(BusinessRuleException.class);

        verify(gateway, never()).send(any());
    }
}
