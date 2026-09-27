package com.pb.crm.commons.autoconfigure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class CrmMessagingEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String SOURCE_NAME = "crmMessagingDefaults";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> defaults = Map.of(
                "spring.rabbitmq.publisher-confirm-type", "correlated",
                "spring.rabbitmq.publisher-returns", "true",
                "spring.rabbitmq.template.mandatory", "true",
                "spring.rabbitmq.listener.simple.default-requeue-rejected", "true",
                "crm.messaging.source", "${spring.application.name:crm-service}"
        );
        environment.getPropertySources().addLast(new MapPropertySource(SOURCE_NAME, defaults));
    }
}
