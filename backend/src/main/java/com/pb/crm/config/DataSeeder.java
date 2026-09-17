package com.pb.crm.config;

import com.pb.crm.agent.Agent;
import com.pb.crm.agent.AgentRepository;
import com.pb.crm.customer.Customer;
import com.pb.crm.customer.CustomerRepository;
import com.pb.crm.ticket.Ticket;
import com.pb.crm.ticket.TicketPriority;
import com.pb.crm.ticket.TicketRepository;
import com.pb.crm.ticket.TicketStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CustomerRepository customerRepository;
    private final AgentRepository agentRepository;
    private final TicketRepository ticketRepository;
    private final TransactionTemplate transactionTemplate;

    public DataSeeder(CustomerRepository customerRepository,
                      AgentRepository agentRepository,
                      TicketRepository ticketRepository,
                      PlatformTransactionManager transactionManager) {
        this.customerRepository = customerRepository;
        this.agentRepository = agentRepository;
        this.ticketRepository = ticketRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (customerRepository.count() > 0) {
            log.info("Banco ja possui dados; carga inicial ignorada");
            return;
        }

        List<Customer> customers = transactionTemplate.execute(status -> customerRepository.saveAll(List.of(
                new Customer("Ana Souza", "ana.souza@empresa1.com", "11988887777", "11122233344"),
                new Customer("Bruno Lima", "bruno.lima@firma2.com", "21977776666", "22233344455"),
                new Customer("Carla Menezes", "carla.menezes@industria3.com", "31966665555", "33344455566")
        )));

        List<Agent> agents = transactionTemplate.execute(status -> agentRepository.saveAll(List.of(
                new Agent("Diego Ferreira", "diego.ferreira@pbcrm.com", "Suporte Tecnico"),
                new Agent("Elisa Prado", "elisa.prado@pbcrm.com", "Financeiro")
        )));

        List<Ticket> tickets = transactionTemplate.execute(status -> ticketRepository.saveAll(List.of(
                new Ticket("Erro ao acessar o sistema",
                        "Cliente recebe erro 500 ao tentar logar na plataforma.",
                        TicketPriority.HIGH, customers.get(0), agents.get(0)),
                new Ticket("Duvida sobre fatura",
                        "Cliente questiona cobranca duplicada na fatura de julho.",
                        TicketPriority.MEDIUM, customers.get(1), null),
                new Ticket("Solicitacao de cancelamento",
                        "Cliente deseja cancelar o plano atual.",
                        TicketPriority.LOW, customers.get(2), agents.get(1))
        )));

        transactionTemplate.executeWithoutResult(status -> {
            Ticket first = ticketRepository.findById(tickets.get(0).getId()).orElseThrow();
            first.changeStatus(TicketStatus.IN_PROGRESS, "atendente iniciou a analise");
            first.addInteraction("Diego Ferreira", "Identificamos o problema, ajuste ja esta em andamento.");
            first.addInteraction("Ana Souza", "Obrigada, aguardo o retorno.");
        });

        transactionTemplate.executeWithoutResult(status -> {
            Ticket third = ticketRepository.findById(tickets.get(2).getId()).orElseThrow();
            third.changeStatus(TicketStatus.IN_PROGRESS, "cancelamento em processamento");
            third.addInteraction("Elisa Prado", "Cancelamento processado com sucesso.");
        });

        transactionTemplate.executeWithoutResult(status -> {
            Ticket third = ticketRepository.findById(tickets.get(2).getId()).orElseThrow();
            third.changeStatus(TicketStatus.RESOLVED, "cliente confirmou o cancelamento");
        });

        log.info("Carga inicial concluida: {} clientes, {} atendentes, {} tickets",
                customers.size(), agents.size(), tickets.size());
    }
}
