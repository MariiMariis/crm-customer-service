package com.pb.crm.sales.domain.reference;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface ReferenceRepository {

    Optional<CompanyRef> findCompany(Long id);

    Map<Long, CompanyRef> findCompanies(Collection<Long> ids);

    boolean upsertCompany(CompanyRef company, Instant occurredAt);

    default void upsertCompany(CompanyRef company) {
        upsertCompany(company, Instant.now());
    }

    Optional<ContactRef> findContact(Long id);

    Map<Long, ContactRef> findContacts(Collection<Long> ids);

    boolean upsertContact(ContactRef contact, Instant occurredAt);

    default void upsertContact(ContactRef contact) {
        upsertContact(contact, Instant.now());
    }

    Optional<ProductRef> findProduct(Long id);

    boolean upsertProduct(ProductRef product, Instant occurredAt);

    default void upsertProduct(ProductRef product) {
        upsertProduct(product, Instant.now());
    }
}
