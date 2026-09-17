package com.pb.crm.persistence;

import com.pb.crm.agent.Agent;
import com.pb.crm.agent.AgentRepository;
import com.pb.crm.config.PersistenceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(PersistenceConfig.class)
class AgentRepositoryTest {

    @Autowired
    private AgentRepository agentRepository;

    @BeforeEach
    void setUp() {
        Agent inactive = new Agent("Paulo Inativo", "paulo@pbcrm.com", "Financeiro");
        inactive.update(inactive.getName(), inactive.getEmail(), inactive.getDepartment(), false);
        agentRepository.saveAll(List.of(
                new Agent("Elisa Prado", "elisa@pbcrm.com", "Financeiro"),
                new Agent("Diego Ferreira", "diego@pbcrm.com", "Suporte Tecnico"),
                inactive
        ));
        agentRepository.flush();
    }

    @Test
    void deveListarApenasAtivosOrdenadosPorNome() {
        assertThat(agentRepository.findByActiveTrueOrderByNameAsc())
                .extracting(Agent::getName)
                .containsExactly("Diego Ferreira", "Elisa Prado");
        assertThat(agentRepository.countByActiveTrue()).isEqualTo(2);
    }

    @Test
    void deveFiltrarPorDepartamentoIgnorandoCaixa() {
        assertThat(agentRepository.findByDepartmentIgnoreCaseOrderByNameAsc("financeiro"))
                .extracting(Agent::getName)
                .containsExactly("Elisa Prado", "Paulo Inativo");
    }

    @Test
    void deveGarantirUnicidadeDeEmail() {
        assertThat(agentRepository.existsByEmailIgnoreCase("ELISA@pbcrm.com")).isTrue();
        assertThatThrownBy(() -> agentRepository.saveAndFlush(new Agent("Duplicada", "elisa@pbcrm.com", "Vendas")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void devePreencherAuditoriaEVersao() {
        Agent agent = agentRepository.findByDepartmentIgnoreCaseOrderByNameAsc("Suporte Tecnico").get(0);

        assertThat(agent.getCreatedAt()).isNotNull();
        assertThat(agent.getCreatedBy()).isEqualTo("system");
        assertThat(agent.getVersion()).isZero();

        agent.update("Diego F.", agent.getEmail(), "Suporte", true);
        Agent updated = agentRepository.saveAndFlush(agent);

        assertThat(updated.getVersion()).isEqualTo(1);
        assertThat(updated.getUpdatedBy()).isEqualTo("system");
    }
}
