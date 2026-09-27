package com.pb.crm.commons.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pb.crm.commons.messaging.DomainEventPublisher;
import com.pb.crm.commons.messaging.MessagingProperties;
import com.pb.crm.commons.messaging.consumer.EventConsumer;
import com.pb.crm.commons.messaging.consumer.ProcessedEventStore;
import com.pb.crm.commons.messaging.consumer.RetryRouter;
import com.pb.crm.commons.messaging.outbox.OutboxDomainEventPublisher;
import com.pb.crm.commons.messaging.outbox.OutboxEventRepository;
import com.pb.crm.commons.messaging.outbox.OutboxRelay;
import com.pb.crm.commons.messaging.topology.MessagingTopology;
import com.pb.crm.commons.messaging.web.MessagingController;
import com.pb.crm.commons.messaging.web.MessagingOperations;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@AutoConfiguration(after = {RabbitAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(prefix = "crm.messaging", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MessagingProperties.class)
@EnableScheduling
public class CrmMessagingAutoConfiguration {

    @Bean
    public MessagingTopology messagingTopology(MessagingProperties properties) {
        return new MessagingTopology(properties);
    }

    @Bean
    public Declarables crmOwnExchange(MessagingTopology topology) {
        return topology.ownExchange();
    }

    @Bean
    public DomainEventPublisher domainEventPublisher(OutboxEventRepository repository,
                                                     ObjectMapper objectMapper,
                                                     MessagingProperties properties) {
        return new OutboxDomainEventPublisher(repository, objectMapper, properties);
    }

    @Bean
    public OutboxRelay outboxRelay(OutboxEventRepository repository,
                                   RabbitTemplate rabbitTemplate,
                                   PlatformTransactionManager transactionManager,
                                   MessagingProperties properties) {
        return new OutboxRelay(repository, rabbitTemplate, new TransactionTemplate(transactionManager), properties);
    }

    @Bean
    public ProcessedEventStore processedEventStore(JdbcTemplate jdbcTemplate) {
        return new ProcessedEventStore(jdbcTemplate);
    }

    @Bean
    public RetryRouter retryRouter(RabbitTemplate rabbitTemplate, MessagingTopology topology) {
        return new RetryRouter(rabbitTemplate, topology);
    }

    @Bean
    public EventConsumer eventConsumer(ObjectMapper objectMapper,
                                       PlatformTransactionManager transactionManager,
                                       ProcessedEventStore processedEventStore,
                                       RetryRouter retryRouter) {
        return new EventConsumer(objectMapper, new TransactionTemplate(transactionManager), processedEventStore, retryRouter);
    }

    @Bean
    public MessagingOperations messagingOperations(MessagingProperties properties,
                                                   MessagingTopology topology,
                                                   OutboxEventRepository outboxRepository,
                                                   AmqpAdmin amqpAdmin,
                                                   RabbitTemplate rabbitTemplate) {
        return new MessagingOperations(properties, topology, outboxRepository, amqpAdmin, rabbitTemplate);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnBean(MessagingOperations.class)
    public MessagingController messagingController(MessagingOperations operations) {
        return new MessagingController(operations);
    }
}
