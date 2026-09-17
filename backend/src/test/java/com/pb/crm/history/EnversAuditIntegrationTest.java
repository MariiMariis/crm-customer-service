package com.pb.crm.history;

import com.pb.crm.agent.AgentService;
import com.pb.crm.agent.dto.AgentRequest;
import com.pb.crm.agent.dto.AgentResponse;
import com.pb.crm.audit.RevisionResponse;
import com.pb.crm.customer.CustomerService;
import com.pb.crm.customer.dto.CustomerRequest;
import com.pb.crm.customer.dto.CustomerResponse;
import com.pb.crm.ticket.TicketPriority;
import com.pb.crm.ticket.TicketService;
import com.pb.crm.ticket.TicketStatus;
import com.pb.crm.ticket.dto.InteractionRequest;
import com.pb.crm.ticket.dto.TicketRequest;
import com.pb.crm.ticket.dto.TicketResponse;
import com.pb.crm.ticket.dto.TicketStatusHistoryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EnversAuditIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private TicketService ticketService;

    private static String unique(String prefix) {
        return prefix + "." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    @Test
    void deveRegistrarRevisoesDeInsercaoAtualizacaoEExclusaoDoCliente() {
        String email = unique("cliente");
        CustomerResponse created = customerService.create(new CustomerRequest("Nome Original", email, "11", null, null));
        customerService.update(created.id(), new CustomerRequest("Nome Alterado", email, "22", null, null));
        customerService.delete(created.id());

        List<RevisionResponse<CustomerResponse>> revisions = customerService.findRevisions(created.id());

        assertThat(revisions).hasSize(3);
        assertThat(revisions).extracting(RevisionResponse::type).containsExactly("INSERT", "UPDATE", "DELETE");
        assertThat(revisions).extracting(RevisionResponse::actor).containsOnly("system");
        assertThat(revisions).extracting(RevisionResponse::revision).isSorted();
        assertThat(revisions.get(0).data().name()).isEqualTo("Nome Original");
        assertThat(revisions.get(0).data().phone()).isEqualTo("11");
        assertThat(revisions.get(1).data().name()).isEqualTo("Nome Alterado");
        assertThat(revisions.get(1).data().phone()).isEqualTo("22");
        assertThat(revisions.get(1).data().updatedAt()).isAfterOrEqualTo(revisions.get(0).data().createdAt());
        assertThat(revisions.get(2).data().name()).isEqualTo("Nome Alterado");
        assertThat(revisions.get(2).timestamp()).isAfterOrEqualTo(revisions.get(0).timestamp());
    }

    @Test
    void deveRegistrarRevisoesDoAtendente() {
        AgentResponse created = agentService.create(new AgentRequest("Atendente", unique("agente"), "Suporte", true, null));
        agentService.update(created.id(), new AgentRequest("Atendente", created.email(), "Financeiro", false, null));

        List<RevisionResponse<AgentResponse>> revisions = agentService.findRevisions(created.id());

        assertThat(revisions).hasSize(2);
        assertThat(revisions.get(0).data().department()).isEqualTo("Suporte");
        assertThat(revisions.get(0).data().active()).isTrue();
        assertThat(revisions.get(1).type()).isEqualTo("UPDATE");
        assertThat(revisions.get(1).data().department()).isEqualTo("Financeiro");
        assertThat(revisions.get(1).data().active()).isFalse();
    }

    @Test
    void deveManterHistoricoDeStatusERevisoesDoTicket() {
        CustomerResponse customer = customerService.create(new CustomerRequest("Cliente Ticket", unique("ticket"), null, null, null));
        TicketResponse ticket = ticketService.create(new TicketRequest("Assunto", "Descricao", TicketPriority.HIGH, customer.id(), null));

        ticketService.changeStatus(ticket.id(), TicketStatus.IN_PROGRESS, "iniciado");
        ticketService.addInteraction(ticket.id(), new InteractionRequest("Diego", "verificando"));
        ticketService.changeStatus(ticket.id(), TicketStatus.RESOLVED, "concluido");

        List<TicketStatusHistoryResponse> history = ticketService.findStatusHistory(ticket.id());
        assertThat(history).extracting(TicketStatusHistoryResponse::toStatus)
                .containsExactly(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED);
        assertThat(history).extracting(TicketStatusHistoryResponse::fromStatus)
                .containsExactly(null, TicketStatus.OPEN, TicketStatus.IN_PROGRESS);
        assertThat(history).extracting(TicketStatusHistoryResponse::reason)
                .containsExactly("abertura do ticket", "iniciado", "concluido");
        assertThat(history).extracting(TicketStatusHistoryResponse::changedBy).containsOnly("system");

        List<RevisionResponse<TicketResponse>> revisions = ticketService.findRevisions(ticket.id());
        assertThat(revisions).hasSize(4);
        assertThat(revisions).extracting(r -> r.data().status())
                .containsExactly(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED);
        assertThat(revisions.get(0).type()).isEqualTo("INSERT");
        assertThat(revisions.get(3).type()).isEqualTo("UPDATE");
        assertThat(revisions.get(3).data().resolvedAt()).isNotNull();
        assertThat(revisions.get(3).data().updatedAt()).isAfterOrEqualTo(revisions.get(0).data().createdAt());
        assertThat(ticketService.findById(ticket.id()).version()).isEqualTo(3L);
    }
}
