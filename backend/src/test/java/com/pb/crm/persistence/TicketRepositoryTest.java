package com.pb.crm.persistence;

import com.pb.crm.agent.Agent;
import com.pb.crm.agent.AgentRepository;
import com.pb.crm.config.PersistenceConfig;
import com.pb.crm.customer.Customer;
import com.pb.crm.customer.CustomerRepository;
import com.pb.crm.ticket.Ticket;
import com.pb.crm.ticket.TicketFilter;
import com.pb.crm.ticket.TicketPriority;
import com.pb.crm.ticket.TicketRepository;
import com.pb.crm.ticket.TicketSpecifications;
import com.pb.crm.ticket.TicketStatus;
import com.pb.crm.ticket.TicketStatusCount;
import com.pb.crm.ticket.TicketStatusHistory;
import com.pb.crm.ticket.TicketStatusHistoryRepository;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(PersistenceConfig.class)
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Customer ana;
    private Customer bruno;
    private Agent diego;

    @BeforeEach
    void setUp() {
        ana = customerRepository.save(new Customer("Ana", "ana@example.com", null, null));
        bruno = customerRepository.save(new Customer("Bruno", "bruno@example.com", null, null));
        diego = agentRepository.save(new Agent("Diego", "diego@pbcrm.com", "Suporte"));

        Ticket t1 = new Ticket("Erro de login", "nao consigo entrar", TicketPriority.HIGH, ana, diego);
        Ticket t2 = new Ticket("Fatura duplicada", "cobranca em dobro", TicketPriority.MEDIUM, bruno, null);
        Ticket t3 = new Ticket("Cancelamento", "quero cancelar", TicketPriority.LOW, ana, diego);
        t3.changeStatus(TicketStatus.IN_PROGRESS, "em analise");
        t3.changeStatus(TicketStatus.RESOLVED, "concluido");
        t1.addInteraction("Diego", "estamos verificando");
        t1.addInteraction("Ana", "obrigada");

        ticketRepository.saveAll(List.of(t1, t2, t3));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void devePersistirInteracoesEHistoricoEmCascata() {
        Ticket ticket = ticketRepository.findWithDetailsById(findIdBySubject("Erro de login")).orElseThrow();

        assertThat(Hibernate.isInitialized(ticket.getInteractions())).isTrue();
        assertThat(ticket.getInteractions()).extracting(i -> i.getAuthor()).containsExactly("Diego", "Ana");
        assertThat(ticket.getInteractions()).allSatisfy(i -> assertThat(i.getCreatedAt()).isNotNull());

        List<TicketStatusHistory> history = statusHistoryRepository.findByTicket_IdOrderByChangedAtAscIdAsc(ticket.getId());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getFromStatus()).isNull();
        assertThat(history.get(0).getToStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(history.get(0).getChangedBy()).isEqualTo("system");
        assertThat(history.get(0).getChangedAt()).isNotNull();
    }

    @Test
    void deveRegistrarCadaTransicaoDeStatusNoHistorico() {
        Long id = findIdBySubject("Cancelamento");

        List<TicketStatusHistory> history = statusHistoryRepository.findByTicket_IdOrderByChangedAtAscIdAsc(id);

        assertThat(history).extracting(TicketStatusHistory::getToStatus)
                .containsExactly(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED);
        assertThat(history).extracting(TicketStatusHistory::getFromStatus)
                .containsExactly(null, TicketStatus.OPEN, TicketStatus.IN_PROGRESS);
        assertThat(history.get(2).getReason()).isEqualTo("concluido");
        assertThat(statusHistoryRepository.countByTicket_Id(id)).isEqualTo(3);
        assertThat(ticketRepository.findById(id).orElseThrow().getResolvedAt()).isNotNull();
    }

    @Test
    void deveCarregarClienteEAtendenteComEntityGraphSemConsultasAdicionais() {
        List<Ticket> open = ticketRepository.findByStatusOrderByCreatedAtDesc(TicketStatus.OPEN);
        entityManager.clear();

        assertThat(open).hasSize(2);
        assertThat(open).allSatisfy(t -> {
            assertThat(Hibernate.isInitialized(t.getCustomer())).isTrue();
            assertThat(t.getCustomer().getName()).isNotBlank();
        });
        assertThat(open).extracting(t -> t.getAgent() == null ? null : Hibernate.isInitialized(t.getAgent()))
                .doesNotContain(Boolean.FALSE);
    }

    @Test
    void deveRemoverInteracoesOrfasAoExcluirTicket() {
        Long id = findIdBySubject("Erro de login");

        ticketRepository.deleteById(id);
        entityManager.flush();

        Long interactions = entityManager.getEntityManager()
                .createQuery("select count(i) from Interaction i where i.ticket.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        assertThat(interactions).isZero();
        assertThat(statusHistoryRepository.countByTicket_Id(id)).isZero();
    }

    @Test
    void deveImpedirExclusaoDeClienteComTicketsPelaChaveEstrangeira() {
        customerRepository.deleteById(ana.getId());

        assertThatThrownBy(() -> customerRepository.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deveContarTicketsAgrupadosPorStatusComProjecao() {
        Map<TicketStatus, Long> counts = ticketRepository.countGroupedByStatus().stream()
                .collect(Collectors.toMap(TicketStatusCount::getStatus, TicketStatusCount::getTotal));

        assertThat(counts).containsEntry(TicketStatus.OPEN, 2L).containsEntry(TicketStatus.RESOLVED, 1L);
        assertThat(counts).doesNotContainKey(TicketStatus.CLOSED);
        assertThat(ticketRepository.countByStatus(TicketStatus.OPEN)).isEqualTo(2);
    }

    @Test
    void deveConsultarPorClienteComJoinFetch() {
        List<Ticket> tickets = ticketRepository.findByCustomerWithRelations(ana.getId());
        entityManager.clear();

        assertThat(tickets).hasSize(2);
        assertThat(tickets).allSatisfy(t -> {
            assertThat(t.getCustomer().getName()).isEqualTo("Ana");
            assertThat(t.getAgent().getName()).isEqualTo("Diego");
        });
        assertThat(ticketRepository.existsByCustomer_Id(ana.getId())).isTrue();
        assertThat(ticketRepository.existsByAgent_Id(diego.getId())).isTrue();
        assertThat(ticketRepository.countByAgent_IdAndStatusIn(diego.getId(), List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS)))
                .isEqualTo(1);
    }

    @Test
    void deveFiltrarComSpecificationsEPaginar() {
        TicketFilter filter = new TicketFilter(null, null, ana.getId(), null, null, null, null);
        Page<Ticket> page = ticketRepository.findAll(TicketSpecifications.withFilter(filter), PageRequest.of(0, 1, Sort.by("subject")));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Ticket::getSubject).containsExactly("Cancelamento");

        TicketFilter byStatusAndPriority = new TicketFilter(TicketStatus.OPEN, TicketPriority.HIGH, null, null, null, null, null);
        assertThat(ticketRepository.findAll(TicketSpecifications.withFilter(byStatusAndPriority)))
                .extracting(Ticket::getSubject).containsExactly("Erro de login");

        TicketFilter bySubject = new TicketFilter(null, null, null, null, "FATURA", null, null);
        assertThat(ticketRepository.findAll(TicketSpecifications.withFilter(bySubject)))
                .extracting(Ticket::getSubject).containsExactly("Fatura duplicada");

        assertThat(ticketRepository.findAll(TicketSpecifications.unassigned()))
                .extracting(Ticket::getSubject).containsExactly("Fatura duplicada");

        assertThat(ticketRepository.findAll(TicketSpecifications.withFilter(TicketFilter.empty()))).hasSize(3);
    }

    @Test
    void deveAtualizarVersaoEDataDeAtualizacaoAoAdicionarInteracao() {
        Long id = findIdBySubject("Fatura duplicada");
        Ticket ticket = ticketRepository.findById(id).orElseThrow();
        Long version = ticket.getVersion();

        ticket.addInteraction("Elisa", "verificando a fatura");
        ticketRepository.saveAndFlush(ticket);
        entityManager.clear();

        Ticket reloaded = ticketRepository.findWithDetailsById(id).orElseThrow();
        assertThat(reloaded.getVersion()).isEqualTo(version + 1);
        assertThat(reloaded.getInteractions()).hasSize(1);
        assertThat(reloaded.getUpdatedAt()).isAfterOrEqualTo(reloaded.getCreatedAt());
    }

    private Long findIdBySubject(String subject) {
        return entityManager.getEntityManager()
                .createQuery("select t.id from Ticket t where t.subject = :subject", Long.class)
                .setParameter("subject", subject)
                .getSingleResult();
    }
}
