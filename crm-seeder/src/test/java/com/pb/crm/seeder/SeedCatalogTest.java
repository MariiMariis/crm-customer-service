package com.pb.crm.seeder;

import com.pb.crm.seeder.SeedCatalog.ActivitySeed;
import com.pb.crm.seeder.SeedCatalog.ItemSeed;
import com.pb.crm.seeder.SeedCatalog.OpportunitySeed;
import com.pb.crm.seeder.SeedCatalog.ProductSeed;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SeedCatalogTest {

    private static final Map<String, ProductSeed> PRODUCTS = SeedCatalog.PRODUCTS.stream()
            .collect(Collectors.toMap(ProductSeed::key, Function.identity()));

    @Test
    void cnpjsAreValidFormattedAndUnique() {
        assertThat(Cnpjs.fromBase("112223330001")).isEqualTo("11.222.333/0001-81");
        Set<String> generated = IntStream.rangeClosed(1, 200).mapToObj(Cnpjs::forCompany).collect(Collectors.toSet());
        assertThat(generated).hasSize(200).allMatch(cnpj -> cnpj.matches("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}"));
    }

    @Test
    void everyReferenceInTheCatalogPointsToAnExistingKey() {
        Set<String> reps = SeedCatalog.SALES_REPS.stream().map(SeedCatalog.RepSeed::key).collect(Collectors.toSet());
        Set<String> companies = SeedCatalog.COMPANIES.stream().map(SeedCatalog.CompanySeed::key).collect(Collectors.toSet());
        Set<String> opportunities = SeedCatalog.OPPORTUNITIES.stream().map(OpportunitySeed::key).collect(Collectors.toSet());
        Set<String> leads = SeedCatalog.LEADS.stream().map(l -> l.firstName() + " " + l.lastName()).collect(Collectors.toSet());

        assertThat(SeedCatalog.SALES_REPS).allMatch(rep -> rep.managerKey() == null || reps.contains(rep.managerKey()));
        assertThat(SeedCatalog.COMPANIES).allMatch(company -> reps.contains(company.ownerKey()) && !company.contacts().isEmpty());
        assertThat(SeedCatalog.LEADS).allMatch(lead -> lead.ownerKey() == null || reps.contains(lead.ownerKey()));
        assertThat(SeedCatalog.OPPORTUNITIES).allMatch(opportunity -> companies.contains(opportunity.companyKey())
                && reps.contains(opportunity.ownerKey())
                && opportunity.items().stream().allMatch(item -> PRODUCTS.containsKey(item.productKey())));
        for (ActivitySeed activity : SeedCatalog.ACTIVITIES) {
            Set<String> pool = switch (activity.relatedType()) {
                case "OPPORTUNITY" -> opportunities;
                case "COMPANY" -> companies;
                case "LEAD" -> leads;
                default -> Set.of();
            };
            assertThat(pool).as(activity.subject()).contains(activity.relatedKey());
            assertThat(reps).contains(activity.ownerKey());
        }
    }

    @Test
    void closedOpportunitiesRespectDiscountRulesAndLostOnesHaveReason() {
        for (OpportunitySeed opportunity : SeedCatalog.OPPORTUNITIES) {
            boolean aboveLimit = opportunity.items().stream().anyMatch(SeedCatalogTest::aboveLimit);
            if ("WON".equals(opportunity.target()) && aboveLimit) {
                assertThat(opportunity.approveDiscount()).as(opportunity.key()).isTrue();
            }
            if ("LOST".equals(opportunity.target())) {
                assertThat(opportunity.lossReason()).as(opportunity.key()).isNotBlank();
            }
        }
        long pendingApprovals = SeedCatalog.OPPORTUNITIES.stream()
                .filter(o -> !o.approveDiscount() && o.items().stream().anyMatch(SeedCatalogTest::aboveLimit))
                .count();
        assertThat(pendingApprovals).isGreaterThanOrEqualTo(2);
    }

    @Test
    void catalogHasTheAgreedMediumVolumeAndUniqueKeys() {
        assertThat(SeedCatalog.SALES_REPS).hasSize(8);
        assertThat(SeedCatalog.COMPANIES).hasSize(15);
        assertThat(SeedCatalog.PRODUCTS).hasSizeGreaterThanOrEqualTo(25);
        assertThat(SeedCatalog.LEADS).hasSize(20);
        assertThat(SeedCatalog.OPPORTUNITIES).hasSize(18);
        assertThat(SeedCatalog.ACTIVITIES).hasSize(30);
        assertThat(new HashSet<>(SeedCatalog.PRODUCTS.stream().map(ProductSeed::key).toList())).hasSize(SeedCatalog.PRODUCTS.size());
        assertThat(SeedCatalog.LEADS.stream().filter(l -> "CONVERTED".equals(l.target()))).allMatch(l -> l.email() != null && l.conversion() != null);
    }

    private static boolean aboveLimit(ItemSeed item) {
        return new BigDecimal(item.discount()).compareTo(new BigDecimal(PRODUCTS.get(item.productKey()).maxDiscount())) > 0;
    }
}
