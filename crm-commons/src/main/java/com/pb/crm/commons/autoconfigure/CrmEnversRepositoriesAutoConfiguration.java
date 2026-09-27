package com.pb.crm.commons.autoconfigure;

import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration(
        after = CrmAuditAutoConfiguration.class,
        before = {HibernateJpaAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class}
)
@ConditionalOnClass(EntityManager.class)
@Import(EnversRepositoriesRegistrar.class)
public class CrmEnversRepositoriesAutoConfiguration {
}
