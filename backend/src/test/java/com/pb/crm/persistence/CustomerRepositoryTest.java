package com.pb.crm.persistence;

import com.pb.crm.config.PersistenceConfig;
import com.pb.crm.customer.Customer;
import com.pb.crm.customer.CustomerRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(PersistenceConfig.class)
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void devePreencherCamposDeAuditoriaEVersaoAoPersistir() {
        Customer saved = customerRepository.saveAndFlush(new Customer("Ana", "ANA@Example.com", " 1199 ", ""));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("ana@example.com");
        assertThat(saved.getPhone()).isEqualTo("1199");
        assertThat(saved.getDocument()).isNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedBy()).isEqualTo("system");
        assertThat(saved.getUpdatedBy()).isEqualTo("system");
        assertThat(saved.getVersion()).isZero();
    }

    @Test
    void deveIncrementarVersaoAoAtualizar() {
        Customer saved = customerRepository.saveAndFlush(new Customer("Bruno", "bruno@example.com", null, null));
        Long firstVersion = saved.getVersion();

        saved.update("Bruno Lima", "bruno@example.com", "2199", "123");
        Customer updated = customerRepository.saveAndFlush(saved);

        assertThat(updated.getVersion()).isEqualTo(firstVersion + 1);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updated.getCreatedAt());
    }

    @Test
    void deveRejeitarEmailDuplicadoPelaRestricaoDeUnicidade() {
        customerRepository.saveAndFlush(new Customer("Carla", "carla@example.com", null, null));

        assertThatThrownBy(() -> customerRepository.saveAndFlush(new Customer("Outra", "CARLA@example.com", null, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deveRejeitarDocumentoDuplicadoPelaRestricaoDeUnicidade() {
        customerRepository.saveAndFlush(new Customer("Doc Um", "doc1@example.com", null, "99988877766"));

        assertThatThrownBy(() -> customerRepository.saveAndFlush(new Customer("Doc Dois", "doc2@example.com", null, "99988877766")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void devePermitirVariosClientesSemDocumento() {
        customerRepository.saveAndFlush(new Customer("Sem Doc Um", "semdoc1@example.com", null, ""));
        customerRepository.saveAndFlush(new Customer("Sem Doc Dois", "semdoc2@example.com", null, null));

        assertThat(customerRepository.count()).isEqualTo(2);
    }

    @Test
    void deveBuscarPorEmailIgnorandoCaixaEVerificarExistencia() {
        Customer saved = customerRepository.saveAndFlush(new Customer("Diego", "diego@example.com", null, null));

        assertThat(customerRepository.findByEmailIgnoreCase("DIEGO@EXAMPLE.COM"))
                .isPresent()
                .get()
                .extracting(Customer::getId)
                .isEqualTo(saved.getId());
        assertThat(customerRepository.existsByEmailIgnoreCase("Diego@Example.com")).isTrue();
        assertThat(customerRepository.existsByEmailIgnoreCaseAndIdNot("diego@example.com", saved.getId())).isFalse();
        assertThat(customerRepository.existsByEmailIgnoreCaseAndIdNot("diego@example.com", saved.getId() + 1)).isTrue();
    }

    @Test
    void deveListarOrdenadoPorNome() {
        customerRepository.saveAll(List.of(
                new Customer("Zeca", "zeca@example.com", null, null),
                new Customer("Alice", "alice@example.com", null, null),
                new Customer("Marcos", "marcos@example.com", null, null)
        ));

        assertThat(customerRepository.findAllByOrderByNameAsc())
                .extracting(Customer::getName)
                .containsExactly("Alice", "Marcos", "Zeca");
    }

    @Test
    void devePesquisarPorNomeEmailOuDocumentoComPaginacao() {
        customerRepository.saveAll(List.of(
                new Customer("Fernanda Costa", "fernanda@empresa.com", null, "111"),
                new Customer("Fernando Souza", "fsouza@outra.com", null, "222"),
                new Customer("Gabriela Rocha", "gabi@empresa.com", null, "333")
        ));
        entityManager.flush();
        entityManager.clear();

        Page<Customer> byName = customerRepository.search("fernan", PageRequest.of(0, 10, Sort.by("name")));
        assertThat(byName.getTotalElements()).isEqualTo(2);
        assertThat(byName.getContent()).extracting(Customer::getName).containsExactly("Fernanda Costa", "Fernando Souza");

        Page<Customer> byEmail = customerRepository.search("empresa.com", PageRequest.of(0, 1, Sort.by("name")));
        assertThat(byEmail.getTotalElements()).isEqualTo(2);
        assertThat(byEmail.getTotalPages()).isEqualTo(2);
        assertThat(byEmail.getContent()).hasSize(1);

        Page<Customer> byDocument = customerRepository.search("333", PageRequest.of(0, 10));
        assertThat(byDocument.getContent()).extracting(Customer::getName).containsExactly("Gabriela Rocha");

        Page<Customer> derived = customerRepository.findByNameContainingIgnoreCase("ROCHA", PageRequest.of(0, 10));
        assertThat(derived.getTotalElements()).isEqualTo(1);
    }
}
