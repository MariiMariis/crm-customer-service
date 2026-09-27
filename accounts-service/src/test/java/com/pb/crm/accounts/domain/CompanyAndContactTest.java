package com.pb.crm.accounts.domain;

import com.pb.crm.accounts.domain.company.BrazilianState;
import com.pb.crm.accounts.domain.company.Cnpj;
import com.pb.crm.accounts.domain.company.Company;
import com.pb.crm.accounts.domain.company.CompanyProfile;
import com.pb.crm.accounts.domain.company.CompanySize;
import com.pb.crm.accounts.domain.company.CompanyType;
import com.pb.crm.accounts.domain.company.Industry;
import com.pb.crm.accounts.domain.contact.Contact;
import com.pb.crm.accounts.domain.contact.ContactProfile;
import com.pb.crm.accounts.domain.contact.DecisionRole;
import com.pb.crm.accounts.domain.salesrep.SalesRepRef;
import com.pb.crm.commons.domain.AuditInfo;
import com.pb.crm.commons.error.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompanyAndContactTest {

    private static CompanyProfile profile(String website) {
        return new CompanyProfile(" Metalurgica Horizonte S.A. ", "Horizonte", Cnpj.of("11222333000181"),
                Industry.MANUFACTURING, CompanySize.LARGE, 850, new BigDecimal("120000000.00"), website,
                null, "Joinville", BrazilianState.SC, CompanyType.PROSPECT, null);
    }

    private static Company persistedCompany(boolean archived) {
        return Company.rehydrate(10L, profile(null), null, archived, null, AuditInfo.empty());
    }

    private static ContactProfile contactProfile() {
        return new ContactProfile("Carla", "Mendes", " Carla.Mendes@Horizonte.com.br ", null, null,
                "Diretora de TI", "Tecnologia", DecisionRole.DECISION_MAKER);
    }

    @Test
    void registerNormalizesProfileAndDisplayNamePrefersTradeName() {
        Company company = Company.register(profile("www.horizonte.com.br"), null);

        assertThat(company.getProfile().legalName()).isEqualTo("Metalurgica Horizonte S.A.");
        assertThat(company.getProfile().website()).isEqualTo("https://www.horizonte.com.br");
        assertThat(company.displayName()).isEqualTo("Horizonte");
    }

    @Test
    void ownerMustBeActiveAndNotArchived() {
        SalesRepRef inactive = new SalesRepRef(5L, "Joao", "joao@pbtech.com.br", false, false);
        SalesRepRef active = new SalesRepRef(6L, "Ana", "ana@pbtech.com.br", true, false);

        assertThatThrownBy(() -> Company.register(profile(null), inactive))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(Company.register(profile(null), active).getOwnerId()).isEqualTo(6L);
    }

    @Test
    void negativeEmployeesOrRevenueAreRejected() {
        CompanyProfile invalid = new CompanyProfile("Empresa", null, Cnpj.of("11222333000181"), Industry.RETAIL,
                CompanySize.SMALL, -1, null, null, null, null, null, CompanyType.PROSPECT, null);

        assertThatThrownBy(() -> Company.register(invalid, null)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void archivedCompanyRejectsNewContactsAndChanges() {
        Company archived = persistedCompany(true);

        assertThatThrownBy(() -> Contact.register(archived, contactProfile(), true))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> archived.update(profile(null))).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void onlyProspectsAndFormerCustomersArePromotedToCustomer() {
        Company prospect = persistedCompany(false);
        assertThat(prospect.promoteToCustomer()).isTrue();
        assertThat(prospect.getProfile().type()).isEqualTo(CompanyType.CUSTOMER);
        assertThat(prospect.pullEvents()).containsExactly(Company.UPDATED);
        assertThat(prospect.promoteToCustomer()).isFalse();

        assertThat(persistedCompany(true).promoteToCustomer()).isFalse();
    }

    @Test
    void contactNormalizesEmailAndLosesPrimaryWhenDeactivatedOrArchived() {
        Contact contact = Contact.register(persistedCompany(false), contactProfile(), true);

        assertThat(contact.getProfile().email()).isEqualTo("carla.mendes@horizonte.com.br");
        assertThat(contact.fullName()).isEqualTo("Carla Mendes");
        assertThat(contact.isPrimary()).isTrue();

        contact.update(contactProfile(), false);
        assertThat(contact.isPrimary()).isFalse();
        assertThatThrownBy(contact::makePrimary).isInstanceOf(BusinessRuleException.class);

        contact.update(contactProfile(), true);
        contact.makePrimary();
        contact.archive();
        assertThat(contact.isPrimary()).isFalse();
    }
}
