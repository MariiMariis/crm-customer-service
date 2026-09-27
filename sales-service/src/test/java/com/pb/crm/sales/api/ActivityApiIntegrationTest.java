package com.pb.crm.sales.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pb.crm.sales.domain.reference.CompanyRef;
import com.pb.crm.sales.domain.reference.ContactRef;
import com.pb.crm.sales.domain.reference.ReferenceRepository;
import com.pb.crm.sales.domain.reference.SalesRepRef;
import com.pb.crm.sales.domain.reference.SalesRepRefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ActivityApiIntegrationTest {

    private static final long OWNER_ID = 1201L;
    private static final long COMPANY_ID = 1301L;
    private static final long ARCHIVED_COMPANY_ID = 1302L;
    private static final long CONTACT_ID = 1401L;

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SalesRepRefRepository salesRepRefRepository;

    @Autowired
    private ReferenceRepository referenceRepository;

    @BeforeEach
    void syncReferences() {
        salesRepRefRepository.upsert(new SalesRepRef(OWNER_ID, "Bianca Rocha", "bianca@pbtech.com.br", null, true, false));
        referenceRepository.upsertCompany(new CompanyRef(COMPANY_ID, "Agro Vale", null, OWNER_ID, false));
        referenceRepository.upsertCompany(new CompanyRef(ARCHIVED_COMPANY_ID, "Antiga SA", null, OWNER_ID, true));
        referenceRepository.upsertContact(new ContactRef(CONTACT_ID, COMPANY_ID, "Helena Duarte", null, true, false));
    }

    private ResultActions send(MockHttpServletRequestBuilder builder, Object body) throws Exception {
        return mockMvc.perform(builder
                .header("X-Actor", "bianca.rocha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private static Map<String, Object> taskPayload(String relatedType, long relatedId, Instant dueAt) {
        Map<String, Object> body = new HashMap<>();
        body.put("type", "TASK");
        body.put("subject", "Enviar proposta comercial");
        body.put("relatedType", relatedType);
        body.put("relatedId", relatedId);
        body.put("ownerId", OWNER_ID);
        body.put("dueAt", dueAt.toString());
        return body;
    }

    private JsonNode create(Map<String, Object> body) throws Exception {
        return objectMapper.readTree(send(post("/api/activities"), body)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    @Test
    void createsActivityRelatedToCompanyWithResolvedNames() throws Exception {
        JsonNode activity = create(taskPayload("COMPANY", COMPANY_ID, Instant.now().plus(Duration.ofDays(2))));

        mockMvc.perform(get("/api/activities/{id}", activity.get("id").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.relatedName").value("Agro Vale"))
                .andExpect(jsonPath("$.ownerName").value("Bianca Rocha"))
                .andExpect(jsonPath("$.priority").value("NORMAL"))
                .andExpect(jsonPath("$.overdue").value(false));

        mockMvc.perform(get("/api/activities").param("relatedType", "COMPANY").param("relatedId", String.valueOf(COMPANY_ID)))
                .andExpect(jsonPath("$.content[*].id").value(hasItem(activity.get("id").intValue())));
    }

    @Test
    void rejectsArchivedOrUnknownRelatedRecordAndInvalidMeeting() throws Exception {
        send(post("/api/activities"), taskPayload("COMPANY", ARCHIVED_COMPANY_ID, Instant.now())).andExpect(status().isConflict());
        send(post("/api/activities"), taskPayload("LEAD", 987654, Instant.now())).andExpect(status().isConflict());

        Map<String, Object> meeting = taskPayload("CONTACT", CONTACT_ID, Instant.now());
        meeting.put("type", "MEETING");
        meeting.put("startsAt", Instant.now().plus(Duration.ofDays(1)).toString());
        meeting.put("endsAt", Instant.now().plus(Duration.ofDays(1)).minus(Duration.ofHours(1)).toString());
        send(post("/api/activities"), meeting).andExpect(status().isConflict());

        Map<String, Object> noDeadline = taskPayload("COMPANY", COMPANY_ID, Instant.now());
        noDeadline.remove("dueAt");
        send(post("/api/activities"), noDeadline).andExpect(status().isBadRequest());
    }

    @Test
    void overdueCallIsCompletedWithOutcomeAndSummaryIsComputed() throws Exception {
        Map<String, Object> call = taskPayload("CONTACT", CONTACT_ID, Instant.now().minus(Duration.ofHours(3)));
        call.put("type", "CALL");
        call.put("subject", "Ligar para alinhar escopo");
        long id = create(call).get("id").asLong();

        mockMvc.perform(get("/api/activities").param("overdue", "true").param("ownerId", String.valueOf(OWNER_ID)))
                .andExpect(jsonPath("$.content[*].id").value(hasItem((int) id)));
        mockMvc.perform(get("/api/activities/summary").param("ownerId", String.valueOf(OWNER_ID)))
                .andExpect(jsonPath("$.overdue").value(greaterThanOrEqualTo(1)));

        send(post("/api/activities/{id}/complete", id), Map.of("outcome", "")).andExpect(status().isBadRequest());
        send(post("/api/activities/{id}/complete", id), Map.of("outcome", "Escopo validado com a TI", "durationMinutes", 25))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.overdue").value(false))
                .andExpect(jsonPath("$.durationMinutes").value(25));

        mockMvc.perform(get("/api/activities/summary").param("ownerId", String.valueOf(OWNER_ID)))
                .andExpect(jsonPath("$.doneLast7Days").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void agendaListsPlannedMeetingsAndRelatedRecordCannotChange() throws Exception {
        Instant start = Instant.now().plus(Duration.ofDays(3)).truncatedTo(ChronoUnit.HOURS);
        Map<String, Object> meeting = taskPayload("COMPANY", COMPANY_ID, start);
        meeting.put("type", "MEETING");
        meeting.put("subject", "Apresentacao da solucao de backup");
        meeting.put("startsAt", start.toString());
        meeting.put("endsAt", start.plus(Duration.ofMinutes(90)).toString());
        meeting.put("location", "Sede do cliente");
        long id = create(meeting).get("id").asLong();

        mockMvc.perform(get("/api/activities/agenda")
                        .param("ownerId", String.valueOf(OWNER_ID))
                        .param("from", start.minus(Duration.ofDays(1)).toString())
                        .param("to", start.plus(Duration.ofDays(1)).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem((int) id)));

        Map<String, Object> moved = new HashMap<>(meeting);
        moved.put("relatedType", "CONTACT");
        moved.put("relatedId", CONTACT_ID);
        send(put("/api/activities/{id}", id), moved).andExpect(status().isConflict());

        send(post("/api/activities/{id}/cancel", id), Map.of("reason", "cliente remarcou"))
                .andExpect(jsonPath("$.status").value("CANCELED"));
        mockMvc.perform(post("/api/activities/{id}/reopen", id)).andExpect(jsonPath("$.status").value("PLANNED"));
        mockMvc.perform(get("/api/activities/{id}/revisions", id)).andExpect(jsonPath("$.length()").value(3));
    }
}
