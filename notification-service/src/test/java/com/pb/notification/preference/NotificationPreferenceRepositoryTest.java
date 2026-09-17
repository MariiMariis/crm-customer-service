package com.pb.notification.preference;

import com.pb.notification.config.PersistenceConfig;
import com.pb.notification.notification.NotificationChannel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(PersistenceConfig.class)
@ActiveProfiles("test")
class NotificationPreferenceRepositoryTest {

    @Autowired
    private NotificationPreferenceRepository repository;

    @Test
    void devePersistirPreferenciaComAuditoriaDeDatas() {
        NotificationPreference saved = repository.saveAndFlush(new NotificationPreference(10L, true, false, true));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(repository.findByCustomerId(10L)).isPresent();
        assertThat(repository.existsByCustomerId(99L)).isFalse();
        assertThat(saved.isEnabled(NotificationChannel.SMS)).isFalse();
        assertThat(saved.isEnabled(NotificationChannel.EMAIL)).isTrue();
    }

    @Test
    void naoDevePermitirDuasPreferenciasParaOMesmoCliente() {
        repository.saveAndFlush(new NotificationPreference(10L, true, true, true));

        assertThatThrownBy(() -> repository.saveAndFlush(new NotificationPreference(10L, false, false, false)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deveContarClientesComEmailDesabilitado() {
        repository.save(new NotificationPreference(1L, false, true, true));
        repository.save(new NotificationPreference(2L, true, true, true));
        repository.save(new NotificationPreference(3L, false, false, false));

        assertThat(repository.countByEmailEnabledFalse()).isEqualTo(2);
    }
}
