package com.pb.crm.catalog.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pb.crm.catalog.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ResultActions send(MockHttpServletRequestBuilder builder, Object body) throws Exception {
        return mockMvc.perform(builder
                .header("X-Actor", "gestor.produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private static Map<String, Object> firewallPayload() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Firewall Fortinet FortiGate 60F");
        body.put("subcategory", "NETWORK");
        body.put("billing", "ONE_TIME");
        body.put("unit", "UNIT");
        body.put("unitPrice", 12500.00);
        body.put("unitCost", 9375.00);
        body.put("maxDiscountPercent", 10);
        body.put("manufacturer", "Fortinet");
        body.put("warrantyMonths", 12);
        return body;
    }

    private JsonNode create(Map<String, Object> body) throws Exception {
        String response = send(post("/api/products"), body)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    @Test
    void createsProductWithGeneratedSkuDerivedCategoryAndMargin() throws Exception {
        JsonNode product = create(firewallPayload());

        mockMvc.perform(get("/api/products/{id}", product.get("id").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value(matchesPattern("HW-NET-\\d{6}")))
                .andExpect(jsonPath("$.category").value("HARDWARE"))
                .andExpect(jsonPath("$.marginPercent").value(25.00))
                .andExpect(jsonPath("$.recurring").value(false))
                .andExpect(jsonPath("$.sellable").value(true))
                .andExpect(jsonPath("$.createdBy").value("gestor.produtos"));

        mockMvc.perform(get("/api/products/sku/{sku}", product.get("sku").asText().toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.get("id").asLong()));
    }

    @Test
    void rejectsBillingIncompatibleWithCategoryAndWarrantyOutsideHardware() throws Exception {
        Map<String, Object> monthlyHardware = firewallPayload();
        monthlyHardware.put("billing", "MONTHLY");
        send(post("/api/products"), monthlyHardware)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(matchesPattern(".*HARDWARE.*")));

        Map<String, Object> softwareWithWarranty = firewallPayload();
        softwareWithWarranty.put("subcategory", "SECURITY");
        softwareWithWarranty.put("billing", "ANNUAL");
        send(post("/api/products"), softwareWithWarranty).andExpect(status().isConflict());

        Map<String, Object> invalid = firewallPayload();
        invalid.put("maxDiscountPercent", 150);
        invalid.put("unitPrice", 0);
        send(post("/api/products"), invalid)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void subcategoryCannotChangeAndPriceUpdateIsVersioned() throws Exception {
        JsonNode product = create(firewallPayload());
        long id = product.get("id").asLong();

        Map<String, Object> newPrice = firewallPayload();
        newPrice.put("unitPrice", 13200.00);
        newPrice.put("version", 0);
        send(put("/api/products/{id}", id), newPrice)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitPrice").value(13200.00))
                .andExpect(jsonPath("$.sku").value(product.get("sku").asText()));

        send(put("/api/products/{id}", id), newPrice).andExpect(status().isConflict());

        Map<String, Object> otherSubcategory = firewallPayload();
        otherSubcategory.put("subcategory", "SERVER");
        send(put("/api/products/{id}", id), otherSubcategory).andExpect(status().isConflict());
    }

    @Test
    void deactivatedProductsAreFilteredAndArchivedAreHidden() throws Exception {
        long deactivatedId = create(firewallPayload()).get("id").asLong();
        long archivedId = create(firewallPayload()).get("id").asLong();

        mockMvc.perform(post("/api/products/{id}/deactivate", deactivatedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sellable").value(false));
        mockMvc.perform(post("/api/products/{id}/archive", archivedId)).andExpect(status().isOk());
        mockMvc.perform(post("/api/products/{id}/activate", archivedId)).andExpect(status().isConflict());

        mockMvc.perform(get("/api/products").param("active", "false").param("category", "HARDWARE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id").value(hasItem((int) deactivatedId)));

        mockMvc.perform(get("/api/products").param("includeArchived", "true").param("q", "fortigate").param("pageSize", "100"))
                .andExpect(jsonPath("$.content[*].id").value(hasItem((int) archivedId)));

        mockMvc.perform(get("/api/products/{id}/revisions", archivedId))
                .andExpect(jsonPath("$[0].type").value("INSERT"))
                .andExpect(jsonPath("$[1].data.archived").value(true));
    }

    @Test
    void exposesTaxonomyForTheFrontend() throws Exception {
        mockMvc.perform(get("/api/products/taxonomy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].category").value("HARDWARE"))
                .andExpect(jsonPath("$[1].allowedBilling[0]").value("ONE_TIME"))
                .andExpect(jsonPath("$[1].hasWarranty").value(true))
                .andExpect(jsonPath("$[0].subcategories").value(hasItem("SECURITY")));
    }
}
