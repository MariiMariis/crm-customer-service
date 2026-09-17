package com.pb.crm.ticket;

import com.pb.crm.common.BusinessRuleException;
import com.pb.crm.customer.Customer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketStatusTransitionTest {

    private static Ticket newTicket() {
        return new Ticket("Assunto", "Descricao", TicketPriority.MEDIUM, new Customer("Ana", "ana@example.com", null, null), null);
    }

    @Test
    void ticketNasceAbertoComPrimeiroRegistroDeHistorico() {
        Ticket ticket = newTicket();

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(ticket.getStatusHistory()).hasSize(1);
        assertThat(ticket.getStatusHistory().get(0).getFromStatus()).isNull();
        assertThat(ticket.getStatusHistory().get(0).getToStatus()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void transicoesPermitidasSeguemOFluxoDeAtendimento() {
        assertThat(TicketStatus.OPEN.canTransitionTo(TicketStatus.IN_PROGRESS)).isTrue();
        assertThat(TicketStatus.IN_PROGRESS.canTransitionTo(TicketStatus.RESOLVED)).isTrue();
        assertThat(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.CLOSED)).isTrue();
        assertThat(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.IN_PROGRESS)).isTrue();
        assertThat(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.OPEN)).isFalse();
        assertThat(TicketStatus.CLOSED.allowedTransitions()).isEmpty();
        assertThat(TicketStatus.CLOSED.isTerminal()).isTrue();
    }

    @Test
    void mudancaDeStatusRegistraHistoricoEMarcaDatas() {
        Ticket ticket = newTicket();

        ticket.changeStatus(TicketStatus.IN_PROGRESS, "iniciando");
        ticket.changeStatus(TicketStatus.RESOLVED, "resolvido");

        assertThat(ticket.getResolvedAt()).isNotNull();
        assertThat(ticket.getStatusHistory()).extracting(TicketStatusHistory::getToStatus)
                .containsExactly(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED);

        ticket.changeStatus(TicketStatus.IN_PROGRESS, "reaberto");
        assertThat(ticket.getResolvedAt()).isNull();

        ticket.changeStatus(TicketStatus.CLOSED, "encerrado");
        assertThat(ticket.getClosedAt()).isNotNull();
    }

    @Test
    void rejeitaTransicaoInvalidaEStatusRepetido() {
        Ticket ticket = newTicket();

        assertThatThrownBy(() -> ticket.changeStatus(TicketStatus.OPEN, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ja esta no status");

        ticket.changeStatus(TicketStatus.CLOSED, "fechado direto");

        assertThatThrownBy(() -> ticket.changeStatus(TicketStatus.OPEN, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("transicao de status invalida");
        assertThat(ticket.getStatusHistory()).hasSize(2);
    }

    @Test
    void ticketFechadoNaoAceitaAlteracoesNemInteracoes() {
        Ticket ticket = newTicket();
        ticket.changeStatus(TicketStatus.CLOSED, null);

        assertThatThrownBy(() -> ticket.addInteraction("Ana", "oi"))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> ticket.update("Novo", "Nova", TicketPriority.LOW, null))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(ticket.getInteractions()).isEmpty();
    }
}
