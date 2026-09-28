package com.pb.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConfigServerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveServirConfiguracaoDoMicrosservicoDeNotificacoes() throws Exception {
        mockMvc.perform(get("/notification-service/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("notification-service"))
                .andExpect(jsonPath("$.propertySources[*].source['crm.notification.dispatch.concurrency']").value(hasItem("2")))
                .andExpect(jsonPath("$.propertySources[*].source['crm.notification.sender-email']").value(hasItem("no-reply@pbtech.com.br")))
                .andExpect(jsonPath("$.propertySources[*].source['platform.config-source']").value(hasItem("config-server")));
    }

    @Test
    void deveServirConfiguracaoCompartilhadaDeMensageria() throws Exception {
        mockMvc.perform(get("/sales-service/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertySources[*].source['spring.rabbitmq.host']").value(hasItem("${RABBITMQ_HOST:localhost}")))
                .andExpect(jsonPath("$.propertySources[*].source['crm.messaging.retry.delays']").value(hasItem("5s,30s,2m")));
    }

    @Test
    void deveExporHealthCheck() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
