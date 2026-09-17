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
                .andExpect(jsonPath("$.propertySources[*].source['app.notifications.dispatch.fixed-delay']").value(hasItem("5000")))
                .andExpect(jsonPath("$.propertySources[*].source['app.notifications.sender-email']").value(hasItem("no-reply@pbcrm.com")))
                .andExpect(jsonPath("$.propertySources[*].source['platform.config-source']").value(hasItem("config-server")));
    }

    @Test
    void deveServirConfiguracaoDoMonolito() throws Exception {
        mockMvc.perform(get("/crm-customer-service/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("crm-customer-service"))
                .andExpect(jsonPath("$.propertySources[*].source['app.notifications.enabled']").value(hasItem("true")))
                .andExpect(jsonPath("$.propertySources[*].source['spring.cloud.discovery.client.simple.instances.notification-service[0].uri']").value(hasItem("http://localhost:8081")));
    }

    @Test
    void deveExporHealthCheck() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
