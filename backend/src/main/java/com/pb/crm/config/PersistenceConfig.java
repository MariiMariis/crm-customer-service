package com.pb.crm.config;

import com.pb.crm.CrmCustomerServiceApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableJpaRepositories(
        basePackageClasses = CrmCustomerServiceApplication.class,
        repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class
)
public class PersistenceConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> java.util.Optional.of(RequestActor.current());
    }
}
