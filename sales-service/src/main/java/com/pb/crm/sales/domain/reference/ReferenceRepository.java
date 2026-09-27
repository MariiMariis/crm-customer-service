package com.pb.crm.sales.domain.reference;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface ReferenceRepository {

    Optional<CompanyRef> findCompany(Long id);

    Map<Long, CompanyRef> findCompanies(Collection<Long> ids);

    void upsertCompany(CompanyRef company);

    Optional<ContactRef> findContact(Long id);

    Map<Long, ContactRef> findContacts(Collection<Long> ids);

    void upsertContact(ContactRef contact);

    Optional<ProductRef> findProduct(Long id);

    void upsertProduct(ProductRef product);
}
