package com.pb.crm.commons.autoconfigure;

import com.pb.crm.commons.actor.RequestActor;
import com.pb.crm.commons.audit.CrmRevisionEntity;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@AutoConfiguration(before = {HibernateJpaAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class})
@ConditionalOnClass(EntityManager.class)
@AutoConfigurationPackage(basePackageClasses = CrmRevisionEntity.class)
@EnableJpaAuditing(auditorAwareRef = "crmAuditorProvider")
public class CrmAuditAutoConfiguration {

    @Bean
    public AuditorAware<String> crmAuditorProvider() {
        return () -> Optional.of(RequestActor.current());
    }
}
