package com.pb.crm.persistence;

import com.pb.crm.customer.Customer;
import com.pb.crm.customer.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class OptimisticLockingIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void segundaEscritaConcorrenteComVersaoDesatualizadaDeveFalhar() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        String email = "lock." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

        Long id = tx.execute(status -> customerRepository.save(new Customer("Original", email, null, null)).getId());

        Customer firstCopy = tx.execute(status -> customerRepository.findById(id).orElseThrow());
        Customer secondCopy = tx.execute(status -> customerRepository.findById(id).orElseThrow());
        assertThat(firstCopy.getVersion()).isZero();
        assertThat(secondCopy.getVersion()).isZero();

        tx.executeWithoutResult(status -> {
            firstCopy.update("Alterado pela primeira sessao", email, "11", null);
            customerRepository.saveAndFlush(firstCopy);
        });

        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            secondCopy.update("Alterado pela segunda sessao", email, "22", null);
            customerRepository.saveAndFlush(secondCopy);
        })).isInstanceOf(OptimisticLockingFailureException.class);

        Customer persisted = tx.execute(status -> customerRepository.findById(id).orElseThrow());
        assertThat(persisted.getName()).isEqualTo("Alterado pela primeira sessao");
        assertThat(persisted.getVersion()).isEqualTo(1L);
    }
}
