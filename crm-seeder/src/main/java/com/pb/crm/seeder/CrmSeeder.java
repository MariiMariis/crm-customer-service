package com.pb.crm.seeder;

import com.fasterxml.jackson.databind.JsonNode;
import com.pb.crm.seeder.SeedCatalog.ActivitySeed;
import com.pb.crm.seeder.SeedCatalog.CompanySeed;
import com.pb.crm.seeder.SeedCatalog.ContactSeed;
import com.pb.crm.seeder.SeedCatalog.ItemSeed;
import com.pb.crm.seeder.SeedCatalog.LeadSeed;
import com.pb.crm.seeder.SeedCatalog.OpportunitySeed;
import com.pb.crm.seeder.SeedCatalog.ProductSeed;
import com.pb.crm.seeder.SeedCatalog.RepSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CrmSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CrmSeeder.class);
    private static final List<String> STAGE_PATH = List.of("PROSPECTING", "QUALIFICATION", "PROPOSAL", "NEGOTIATION");

    private final CrmApiClient api;
    private final Map<String, Long> reps = new LinkedHashMap<>();
    private final Map<String, Long> products = new LinkedHashMap<>();
    private final Map<String, Long> companies = new LinkedHashMap<>();
    private final Map<String, List<Long>> contacts = new HashMap<>();
    private final Map<String, Long> leads = new HashMap<>();
    private final Map<String, Long> opportunities = new HashMap<>();

    public CrmSeeder(CrmApiClient api) {
        this.api = api;
    }

    @Override
    public void run(ApplicationArguments args) {
        long existing = api.total("/api/sales-reps?includeArchived=true&pageSize=1");
        if (existing > 0) {
            log.warn("A base ja possui {} vendedor(es); carga inicial ignorada. Para recarregar, pare os servicos, "
                    + "rode 'docker compose down -v' e 'docker compose up -d' e execute a carga novamente.", existing);
            return;
        }
        Instant start = Instant.now();
        seedSalesReps();
        seedProducts();
        seedCompaniesAndContacts();
        seedLeads();
        seedOpportunities();
        seedActivities();
        log.info("Carga inicial concluida em {}s: {} vendedores, {} produtos, {} empresas, {} contatos, {} leads, "
                        + "{} oportunidades e {} atividades",
                Duration.between(start, Instant.now()).toSeconds(), reps.size(), products.size(), companies.size(),
                contacts.values().stream().mapToInt(List::size).sum(), leads.size(), opportunities.size(),
                SeedCatalog.ACTIVITIES.size());
    }

    private void seedSalesReps() {
        for (RepSeed rep : SeedCatalog.SALES_REPS) {
            Map<String, Object> body = new HashMap<>();
            body.put("name", rep.name());
            body.put("email", rep.email());
            body.put("phone", rep.phone());
            body.put("team", rep.team());
            body.put("role", rep.role());
            body.put("monthlyQuota", rep.quota());
            body.put("managerId", rep.managerKey() == null ? null : reps.get(rep.managerKey()));
            reps.put(rep.key(), api.post("/api/sales-reps", body).get("id").asLong());
        }
        log.info("{} vendedores cadastrados no team-service", reps.size());
    }

    private void seedProducts() {
        for (ProductSeed product : SeedCatalog.PRODUCTS) {
            Map<String, Object> body = new HashMap<>();
            body.put("name", product.name());
            body.put("description", product.description());
            body.put("subcategory", product.subcategory());
            body.put("billing", product.billing());
            body.put("unit", product.unit());
            body.put("unitPrice", SeedCatalog.decimal(product.price()));
            body.put("unitCost", SeedCatalog.decimal(product.cost()));
            body.put("maxDiscountPercent", SeedCatalog.decimal(product.maxDiscount()));
            body.put("manufacturer", product.manufacturer());
            body.put("warrantyMonths", product.warrantyMonths());
            products.put(product.key(), api.post("/api/products", body).get("id").asLong());
        }
        log.info("{} produtos cadastrados no catalog-service", products.size());
    }

    private void seedCompaniesAndContacts() {
        int sequence = 1;
        for (CompanySeed company : SeedCatalog.COMPANIES) {
            Map<String, Object> body = new HashMap<>();
            body.put("legalName", company.legalName());
            body.put("tradeName", company.tradeName());
            body.put("cnpj", Cnpjs.forCompany(sequence++));
            body.put("industry", company.industry());
            body.put("size", company.size());
            body.put("employees", company.employees());
            body.put("annualRevenue", SeedCatalog.decimal(company.annualRevenue()));
            body.put("website", company.website());
            body.put("phone", company.phone());
            body.put("city", company.city());
            body.put("state", company.state());
            body.put("type", company.type());
            body.put("ownerId", reps.get(company.ownerKey()));
            long companyId = api.post("/api/companies", body).get("id").asLong();
            companies.put(company.key(), companyId);
            List<Long> ids = new ArrayList<>();
            for (ContactSeed contact : company.contacts()) {
                Map<String, Object> contactBody = new HashMap<>();
                contactBody.put("companyId", companyId);
                contactBody.put("firstName", contact.firstName());
                contactBody.put("lastName", contact.lastName());
                contactBody.put("email", contact.emailUser() + "@" + company.website());
                contactBody.put("phone", contact.phone());
                contactBody.put("mobile", contact.phone());
                contactBody.put("jobTitle", contact.jobTitle());
                contactBody.put("department", contact.department());
                contactBody.put("decisionRole", contact.decisionRole());
                ids.add(api.post("/api/contacts", contactBody).get("id").asLong());
            }
            contacts.put(company.key(), ids);
        }
        log.info("{} empresas e {} contatos cadastrados no accounts-service", companies.size(),
                contacts.values().stream().mapToInt(List::size).sum());
    }

    private void seedLeads() {
        int conversionSequence = 100;
        for (LeadSeed lead : SeedCatalog.LEADS) {
            Map<String, Object> body = new HashMap<>();
            body.put("firstName", lead.firstName());
            body.put("lastName", lead.lastName());
            body.put("email", lead.email());
            body.put("phone", lead.phone());
            body.put("companyName", lead.companyName());
            body.put("jobTitle", lead.jobTitle());
            body.put("source", lead.source());
            body.put("estimatedValue", lead.estimatedValue());
            body.put("ownerId", lead.ownerKey() == null ? null : reps.get(lead.ownerKey()));
            long id = api.post("/api/leads", body).get("id").asLong();
            leads.put(lead.firstName() + " " + lead.lastName(), id);
            switch (lead.target()) {
                case "CONTACTED" -> api.post("/api/leads/{id}/contacted", null, id);
                case "QUALIFIED" -> qualify(id);
                case "UNQUALIFIED" -> api.post("/api/leads/{id}/disqualify", Map.of("reason", lead.disqualifyReason()), id);
                case "CONVERTED" -> convert(id, lead, conversionSequence++);
                default -> {
                }
            }
        }
        log.info("{} leads cadastrados no sales-service (3 convertidos pela saga com o accounts-service)", leads.size());
    }

    private void qualify(long leadId) {
        api.post("/api/leads/{id}/contacted", null, leadId);
        api.post("/api/leads/{id}/qualify", null, leadId);
    }

    private void convert(long leadId, LeadSeed lead, int cnpjSequence) {
        qualify(leadId);
        Map<String, Object> conversion = new HashMap<>();
        conversion.put("cnpj", Cnpjs.forCompany(cnpjSequence));
        conversion.put("industry", lead.conversion().industry());
        conversion.put("companySize", lead.conversion().companySize());
        conversion.put("city", lead.conversion().city());
        conversion.put("state", lead.conversion().state());
        conversion.put("createOpportunity", true);
        conversion.put("opportunityTitle", lead.conversion().opportunityTitle());
        conversion.put("expectedCloseDate", LocalDate.now().plusDays(40).toString());
        api.post("/api/leads/{id}/convert", conversion, leadId);
        JsonNode converted = api.waitUntil("conversao do lead #" + leadId,
                () -> api.get("/api/leads/{id}", leadId),
                node -> !"CONVERTING".equals(node.path("status").asText()));
        if (!"CONVERTED".equals(converted.path("status").asText())) {
            throw new IllegalStateException("a saga de conversao do lead %d falhou: %s"
                    .formatted(leadId, converted.path("conversionFailureReason").asText()));
        }
    }

    private void seedOpportunities() {
        for (OpportunitySeed seed : SeedCatalog.OPPORTUNITIES) {
            long ownerId = reps.get(seed.ownerKey());
            List<Long> companyContacts = contacts.get(seed.companyKey());
            Map<String, Object> body = new HashMap<>();
            body.put("title", seed.title());
            body.put("companyId", companies.get(seed.companyKey()));
            body.put("contactId", companyContacts.get(Math.min(seed.contactIndex(), companyContacts.size() - 1)));
            body.put("ownerId", ownerId);
            body.put("expectedCloseDate", LocalDate.now().plusDays(seed.closeInDays()).toString());
            body.put("contractTermMonths", seed.termMonths());
            long id = api.post("/api/opportunities", body).get("id").asLong();
            opportunities.put(seed.key(), id);
            JsonNode current = null;
            for (ItemSeed item : seed.items()) {
                current = api.post("/api/opportunities/{id}/items", Map.of(
                        "productId", products.get(item.productKey()),
                        "quantity", item.quantity(),
                        "discountPercent", SeedCatalog.decimal(item.discount())), id);
            }
            if (seed.approveDiscount() && current != null && "PENDING".equals(current.path("discountApproval").asText())) {
                api.post("/api/opportunities/{id}/discount-decision", Map.of(
                        "approverId", managerOf(seed.ownerKey()),
                        "approved", true,
                        "comment", "volume estrategico aprovado pela gestao"), id);
            }
            advance(id, seed);
        }
        log.info("{} oportunidades cadastradas no sales-service (com itens, aprovacoes, ganhos e perdas)", opportunities.size());
    }

    private void advance(long id, OpportunitySeed seed) {
        String target = switch (seed.target()) {
            case "WON" -> "NEGOTIATION";
            case "LOST" -> "PROPOSAL";
            default -> seed.target();
        };
        for (String stage : STAGE_PATH.subList(1, STAGE_PATH.indexOf(target) + 1)) {
            api.post("/api/opportunities/{id}/stage", Map.of("stage", stage), id);
        }
        if ("WON".equals(seed.target())) {
            api.post("/api/opportunities/{id}/win", null, id);
        } else if ("LOST".equals(seed.target())) {
            api.post("/api/opportunities/{id}/lose", Map.of("reason", seed.lossReason()), id);
        }
    }

    private void seedActivities() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        for (ActivitySeed seed : SeedCatalog.ACTIVITIES) {
            Map<String, Object> body = new HashMap<>();
            body.put("type", seed.type());
            body.put("subject", seed.subject());
            body.put("relatedType", seed.relatedType());
            body.put("relatedId", relatedId(seed));
            body.put("ownerId", reps.get(seed.ownerKey()));
            body.put("priority", seed.dueInHours() < 0 && seed.outcome() == null ? "HIGH" : "NORMAL");
            Instant due = now.plus(Duration.ofHours(seed.dueInHours()));
            if ("MEETING".equals(seed.type())) {
                body.put("startsAt", due.toString());
                body.put("endsAt", due.plus(Duration.ofMinutes(seed.meetingMinutes())).toString());
                body.put("location", seed.location());
            } else {
                body.put("dueAt", due.toString());
            }
            long id = api.post("/api/activities", body).get("id").asLong();
            if (seed.outcome() != null) {
                Map<String, Object> completion = new HashMap<>();
                completion.put("outcome", seed.outcome());
                completion.put("durationMinutes", seed.callMinutes());
                api.post("/api/activities/{id}/complete", completion, id);
            }
        }
        log.info("{} atividades cadastradas (algumas concluidas e outras atrasadas)", SeedCatalog.ACTIVITIES.size());
    }

    private long relatedId(ActivitySeed seed) {
        Long id = switch (seed.relatedType()) {
            case "OPPORTUNITY" -> opportunities.get(seed.relatedKey());
            case "COMPANY" -> companies.get(seed.relatedKey());
            case "LEAD" -> leads.get(seed.relatedKey());
            default -> null;
        };
        if (id == null) {
            throw new IllegalStateException("registro relacionado nao encontrado na carga: " + seed.relatedKey());
        }
        return id;
    }

    private long managerOf(String repKey) {
        String managerKey = SeedCatalog.SALES_REPS.stream()
                .filter(rep -> rep.key().equals(repKey))
                .findFirst()
                .map(RepSeed::managerKey)
                .orElseThrow();
        return reps.get(managerKey);
    }
}
