package com.pb.crm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CrmApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveListarClientesSeedados() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ana Souza"))
                .andExpect(jsonPath("$[0].email").value("ana.souza@example.com"));
    }

    @Test
    void deveCriarClienteAtendenteEAbrirTicketComInteracao() throws Exception {
        // 1) cria um cliente
        String customerJson = """
                {"name":"Joana Ribeiro","email":"joana.ribeiro@example.com","phone":"11999990000","document":"12345678900"}
                """;
        String customerResponse = mockMvc.perform(post("/api/customers")
                        .contentType("application/json")
                        .content(customerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Joana Ribeiro"))
                .andReturn().getResponse().getContentAsString();
        long customerId = extractId(customerResponse);

        // 2) cria um atendente
        String agentJson = """
                {"name":"Marcos Silva","email":"marcos.silva@pbcrm.com","department":"Suporte","active":true}
                """;
        String agentResponse = mockMvc.perform(post("/api/agents")
                        .contentType("application/json")
                        .content(agentJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long agentId = extractId(agentResponse);

        // 3) abre um ticket para o cliente, atribuido ao atendente (equivalente ao fluxo do diagrama de sequencia)
        String ticketJson = """
                {"subject":"Sistema fora do ar","description":"Cliente nao consegue acessar o painel","priority":"HIGH","customerId":%d,"agentId":%d}
                """.formatted(customerId, agentId);
        String ticketResponse = mockMvc.perform(post("/api/tickets")
                        .contentType("application/json")
                        .content(ticketJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andReturn().getResponse().getContentAsString();
        long ticketId = extractId(ticketResponse);

        // 4) atendente registra uma interacao no ticket
        String interactionJson = """
                {"author":"Marcos Silva","message":"Estamos verificando o problema."}
                """;
        mockMvc.perform(post("/api/tickets/" + ticketId + "/interactions")
                        .contentType("application/json")
                        .content(interactionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author").value("Marcos Silva"));

        // 5) atendente move o ticket para IN_PROGRESS
        mockMvc.perform(patch("/api/tickets/" + ticketId + "/status")
                        .contentType("application/json")
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // 6) busca o ticket e confere que a interacao e o status foram persistidos
        mockMvc.perform(get("/api/tickets/" + ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.interactions.length()").value(1))
                .andExpect(jsonPath("$.interactions[0].message").value("Estamos verificando o problema."));
    }

    @Test
    void deveRetornar404AoBuscarTicketInexistente() throws Exception {
        mockMvc.perform(get("/api/tickets/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void deveRetornar400AoCriarClienteComEmailInvalido() throws Exception {
        String invalidJson = """
                {"name":"Sem Email Valido","email":"nao-e-um-email","phone":"","document":""}
                """;
        mockMvc.perform(post("/api/customers")
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    private static long extractId(String json) {
        var matcher = java.util.regex.Pattern.compile("\"id\":(\\d+)").matcher(json);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        throw new IllegalStateException("id nao encontrado no JSON: " + json);
    }
}
