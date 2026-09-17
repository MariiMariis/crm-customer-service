package com.pb.crm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CrmApiIntegrationTest {

    private static final String JSON = "application/json";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveListarClientesSeedados() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(hasItem("Ana Souza")))
                .andExpect(jsonPath("$[*].email").value(hasItem("ana.souza@empresa1.com")));
    }

    @Test
    void deveExporEstatisticasDeTicketsPorStatus() throws Exception {
        mockMvc.perform(get("/api/tickets/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.byStatus.OPEN").exists())
                .andExpect(jsonPath("$.byStatus.CLOSED").exists());
    }

    @Test
    void deveExecutarFluxoCompletoComHistoricoDeStatusERevisoes() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        String customerJson = """
                {"name":"Joana Ribeiro","email":"joana.%s@example.com","phone":"11999990000","document":"doc-%s"}
                """.formatted(suffix, suffix);
        String customerResponse = mockMvc.perform(post("/api/customers")
                        .contentType(JSON)
                        .header("X-Actor", "marcos")
                        .content(customerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Joana Ribeiro"))
                .andExpect(jsonPath("$.createdBy").value("marcos"))
                .andExpect(jsonPath("$.version").value(0))
                .andReturn().getResponse().getContentAsString();
        long customerId = extractId(customerResponse);

        String agentJson = """
                {"name":"Marcos Silva","email":"marcos.%s@pbcrm.com","department":"Suporte","active":true}
                """.formatted(suffix);
        String agentResponse = mockMvc.perform(post("/api/agents")
                        .contentType(JSON)
                        .content(agentJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long agentId = extractId(agentResponse);

        String ticketJson = """
                {"subject":"Sistema fora do ar","description":"Cliente nao consegue acessar o painel","priority":"HIGH","customerId":%d,"agentId":%d}
                """.formatted(customerId, agentId);
        String ticketResponse = mockMvc.perform(post("/api/tickets")
                        .contentType(JSON)
                        .header("X-Actor", "marcos")
                        .content(ticketJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.customerName").value("Joana Ribeiro"))
                .andExpect(jsonPath("$.agentName").value("Marcos Silva"))
                .andReturn().getResponse().getContentAsString();
        long ticketId = extractId(ticketResponse);

        mockMvc.perform(post("/api/tickets/" + ticketId + "/interactions")
                        .contentType(JSON)
                        .content("{\"author\":\"Marcos Silva\",\"message\":\"Estamos verificando o problema.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author").value("Marcos Silva"));

        mockMvc.perform(patch("/api/tickets/" + ticketId + "/status")
                        .contentType(JSON)
                        .header("X-Actor", "marcos")
                        .content("{\"status\":\"IN_PROGRESS\",\"reason\":\"analise iniciada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(get("/api/tickets/" + ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.interactions", hasSize(1)))
                .andExpect(jsonPath("$.interactions[0].message").value("Estamos verificando o problema."));

        mockMvc.perform(get("/api/tickets/" + ticketId + "/status-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].fromStatus").doesNotExist())
                .andExpect(jsonPath("$[0].toStatus").value("OPEN"))
                .andExpect(jsonPath("$[1].fromStatus").value("OPEN"))
                .andExpect(jsonPath("$[1].toStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[1].reason").value("analise iniciada"))
                .andExpect(jsonPath("$[1].changedBy").value("marcos"));

        mockMvc.perform(get("/api/tickets/" + ticketId + "/revisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].type").value("INSERT"))
                .andExpect(jsonPath("$[0].actor").value("marcos"))
                .andExpect(jsonPath("$[0].data.status").value("OPEN"))
                .andExpect(jsonPath("$[-1].data.status").value("IN_PROGRESS"));

        mockMvc.perform(get("/api/customers/" + customerId + "/revisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("INSERT"))
                .andExpect(jsonPath("$[0].actor").value("marcos"));

        mockMvc.perform(get("/api/tickets/search")
                        .param("customerId", String.valueOf(customerId))
                        .param("status", "IN_PROGRESS")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(ticketId))
                .andExpect(jsonPath("$.content[0].customerName").value("Joana Ribeiro"));

        mockMvc.perform(get("/api/customers/search").param("q", "joana." + suffix))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(customerId));
    }

    @Test
    void deveRejeitarTransicaoDeStatusInvalida() throws Exception {
        long ticketId = createTicketForNewCustomer();

        mockMvc.perform(patch("/api/tickets/" + ticketId + "/status")
                        .contentType(JSON)
                        .content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/tickets/" + ticketId + "/status")
                        .contentType(JSON)
                        .content("{\"status\":\"OPEN\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Business Rule Violation"));

        mockMvc.perform(post("/api/tickets/" + ticketId + "/interactions")
                        .contentType(JSON)
                        .content("{\"author\":\"Alguem\",\"message\":\"tentativa apos fechamento\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void deveImpedirExclusaoDeClienteComTicketsVinculados() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        long customerId = createCustomer(suffix);
        createTicket(customerId);

        mockMvc.perform(delete("/api/customers/" + customerId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Data Integrity Violation"));

        mockMvc.perform(get("/api/customers/" + customerId))
                .andExpect(status().isOk());
    }

    @Test
    void deveRejeitarEmailDuplicadoDeCliente() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        createCustomer(suffix);

        String duplicated = """
                {"name":"Outro Nome","email":"CLIENTE.%s@EXAMPLE.COM","phone":"","document":""}
                """.formatted(suffix);
        mockMvc.perform(post("/api/customers").contentType(JSON).content(duplicated))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Business Rule Violation"));
    }

    @Test
    void deveDetectarVersaoDesatualizadaNaAtualizacao() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        long customerId = createCustomer(suffix);

        String update = """
                {"name":"Nome Atualizado","email":"cliente.%s@example.com","phone":"","document":"","version":0}
                """.formatted(suffix);
        mockMvc.perform(put("/api/customers/" + customerId).contentType(JSON).content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.name").value("Nome Atualizado"));

        mockMvc.perform(put("/api/customers/" + customerId).contentType(JSON).content(update))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Concurrent Modification"));

        mockMvc.perform(get("/api/customers/" + customerId + "/revisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].type").value("UPDATE"))
                .andExpect(jsonPath("$[1].data.name").value("Nome Atualizado"));
    }

    @Test
    void deveRetornar404AoBuscarTicketInexistente() throws Exception {
        mockMvc.perform(get("/api/tickets/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));

        mockMvc.perform(get("/api/tickets/999999/status-history"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoCriarClienteComEmailInvalido() throws Exception {
        String invalidJson = """
                {"name":"Sem Email Valido","email":"nao-e-um-email","phone":"","document":""}
                """;
        mockMvc.perform(post("/api/customers").contentType(JSON).content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void deveRetornar400ParaStatusInvalidoNoFiltro() throws Exception {
        mockMvc.perform(get("/api/tickets").param("status", "INEXISTENTE"))
                .andExpect(status().isBadRequest());
    }

    private long createCustomer(String suffix) throws Exception {
        String json = """
                {"name":"Cliente %s","email":"cliente.%s@example.com","phone":"","document":""}
                """.formatted(suffix, suffix);
        String response = mockMvc.perform(post("/api/customers").contentType(JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return extractId(response);
    }

    private long createTicket(long customerId) throws Exception {
        String json = """
                {"subject":"Ticket de teste","description":"Descricao","priority":"LOW","customerId":%d}
                """.formatted(customerId);
        String response = mockMvc.perform(post("/api/tickets").contentType(JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return extractId(response);
    }

    private long createTicketForNewCustomer() throws Exception {
        return createTicket(createCustomer(UUID.randomUUID().toString().substring(0, 8)));
    }

    private static long extractId(String json) {
        var matcher = java.util.regex.Pattern.compile("\"id\":(\\d+)").matcher(json);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        throw new IllegalStateException("id nao encontrado no JSON: " + json);
    }
}
