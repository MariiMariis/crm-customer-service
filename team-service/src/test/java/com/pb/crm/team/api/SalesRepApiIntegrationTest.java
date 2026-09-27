package com.pb.crm.team.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SalesRepApiIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID().toString().substring(0, 8) + "@pbtech.com.br";
    }

    private ResultActions postJson(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .header("X-Actor", "gestora.comercial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private JsonNode createManager() throws Exception {
        String body = postJson("/api/sales-reps", Map.of(
                "name", "Marina Costa",
                "email", uniqueEmail("marina"),
                "team", "FIELD_SALES",
                "role", "MANAGER"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private JsonNode createRep(long managerId) throws Exception {
        String body = postJson("/api/sales-reps", Map.of(
                "name", "Joao Lima",
                "email", uniqueEmail("joao"),
                "phone", "11988887777",
                "team", "INSIDE_SALES",
                "role", "REP",
                "monthlyQuota", 80000,
                "managerId", managerId))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    @Test
    void createsRepLinkedToManagerAndRecordsAuditActor() throws Exception {
        JsonNode manager = createManager();
        JsonNode rep = createRep(manager.get("id").asLong());

        mockMvc.perform(get("/api/sales-reps/{id}", rep.get("id").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managerId").value(manager.get("id").asLong()))
                .andExpect(jsonPath("$.managerName").value("Marina Costa"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdBy").value("gestora.comercial"))
                .andExpect(jsonPath("$.version").value(0));

        mockMvc.perform(get("/api/sales-reps/{id}/revisions", rep.get("id").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("INSERT"))
                .andExpect(jsonPath("$[0].actor").value("gestora.comercial"));
    }

    @Test
    void rejectsInvalidPayloadDuplicateEmailAndRepWithoutQuota() throws Exception {
        postJson("/api/sales-reps", Map.of("email", "invalido", "team", "INSIDE_SALES", "role", "REP"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        JsonNode manager = createManager();
        postJson("/api/sales-reps", Map.of(
                "name", "Outra Pessoa",
                "email", manager.get("email").asText(),
                "team", "FIELD_SALES",
                "role", "MANAGER"))
                .andExpect(status().isConflict());

        postJson("/api/sales-reps", Map.of(
                "name", "Sem Meta",
                "email", uniqueEmail("semmeta"),
                "team", "INSIDE_SALES",
                "role", "REP"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("a meta mensal e obrigatoria para vendedores"));
    }

    @Test
    void rejectsUpdateWithStaleVersion() throws Exception {
        JsonNode manager = createManager();
        long id = manager.get("id").asLong();

        mockMvc.perform(put("/api/sales-reps/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Marina Costa Souza",
                                "email", manager.get("email").asText(),
                                "team", "FIELD_SALES",
                                "role", "MANAGER",
                                "version", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(put("/api/sales-reps/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Marina C.",
                                "email", manager.get("email").asText(),
                                "team", "FIELD_SALES",
                                "role", "MANAGER",
                                "version", 0))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Concurrent Modification"));
    }

    @Test
    void managerWithActiveSubordinatesCannotBeArchivedUntilTheyAreArchived() throws Exception {
        JsonNode manager = createManager();
        long managerId = manager.get("id").asLong();
        long repId = createRep(managerId).get("id").asLong();

        mockMvc.perform(post("/api/sales-reps/{id}/archive", managerId))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/sales-reps/{id}/archive", repId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/sales-reps").param("managerId", String.valueOf(managerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id").value(not(hasItem((int) repId))));

        mockMvc.perform(get("/api/sales-reps")
                        .param("managerId", String.valueOf(managerId))
                        .param("includeArchived", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id").value(hasItem((int) repId)));

        mockMvc.perform(post("/api/sales-reps/{id}/archive", managerId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/sales-reps/{id}/restore", repId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(false))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void searchFiltersByTeamAndTermWithPagination() throws Exception {
        JsonNode manager = createManager();
        createRep(manager.get("id").asLong());

        mockMvc.perform(get("/api/sales-reps")
                        .param("team", "INSIDE_SALES")
                        .param("q", "joao")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.content[0].team").value("INSIDE_SALES"));

        mockMvc.perform(get("/api/sales-reps").param("pageSize", "500"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/sales-reps/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
