package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.sales.domain.reference.CompanyRef;
import com.pb.crm.sales.domain.reference.ContactRef;
import com.pb.crm.sales.domain.reference.ProductRef;
import com.pb.crm.sales.domain.reference.ReferenceRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReferenceRepositoryAdapter implements ReferenceRepository {

    private final SpringDataCompanyRefRepository companies;
    private final SpringDataContactRefRepository contacts;
    private final SpringDataProductRefRepository products;

    ReferenceRepositoryAdapter(SpringDataCompanyRefRepository companies,
                               SpringDataContactRefRepository contacts,
                               SpringDataProductRefRepository products) {
        this.companies = companies;
        this.contacts = contacts;
        this.products = products;
    }

    @Override
    public Optional<CompanyRef> findCompany(Long id) {
        return companies.findById(id).map(ReferenceRepositoryAdapter::toDomain);
    }

    @Override
    public Map<Long, CompanyRef> findCompanies(Collection<Long> ids) {
        Map<Long, CompanyRef> result = new HashMap<>();
        if (!ids.isEmpty()) {
            companies.findAllById(ids).forEach(entity -> result.put(entity.getId(), toDomain(entity)));
        }
        return result;
    }

    @Override
    public boolean upsertCompany(CompanyRef company, Instant occurredAt) {
        CompanyRefJpaEntity entity = companies.findById(company.id()).orElseGet(() -> new CompanyRefJpaEntity(company.id()));
        if (entity.isStaleComparedTo(occurredAt)) {
            return false;
        }
        entity.apply(company.displayName(), company.cnpj(), company.ownerId(), company.archived(), occurredAt);
        companies.save(entity);
        return true;
    }

    @Override
    public Optional<ContactRef> findContact(Long id) {
        return contacts.findById(id).map(ReferenceRepositoryAdapter::toDomain);
    }

    @Override
    public Map<Long, ContactRef> findContacts(Collection<Long> ids) {
        Map<Long, ContactRef> result = new HashMap<>();
        if (!ids.isEmpty()) {
            contacts.findAllById(ids).forEach(entity -> result.put(entity.getId(), toDomain(entity)));
        }
        return result;
    }

    @Override
    public boolean upsertContact(ContactRef contact, Instant occurredAt) {
        ContactRefJpaEntity entity = contacts.findById(contact.id()).orElseGet(() -> new ContactRefJpaEntity(contact.id()));
        if (entity.isStaleComparedTo(occurredAt)) {
            return false;
        }
        entity.apply(contact.companyId(), contact.fullName(), contact.email(), contact.active(), contact.archived(), occurredAt);
        contacts.save(entity);
        return true;
    }

    @Override
    public Optional<ProductRef> findProduct(Long id) {
        return products.findById(id).map(ReferenceRepositoryAdapter::toDomain);
    }

    @Override
    public boolean upsertProduct(ProductRef product, Instant occurredAt) {
        ProductRefJpaEntity entity = products.findById(product.id()).orElseGet(() -> new ProductRefJpaEntity(product.id()));
        if (entity.isStaleComparedTo(occurredAt)) {
            return false;
        }
        entity.apply(product.sku(), product.name(), product.category(), product.billing(), product.unitPrice(),
                product.maxDiscountPercent(), product.active(), product.archived(), occurredAt);
        products.save(entity);
        return true;
    }

    private static CompanyRef toDomain(CompanyRefJpaEntity entity) {
        return new CompanyRef(entity.getId(), entity.getDisplayName(), entity.getCnpj(), entity.getOwnerId(), entity.isArchived());
    }

    private static ContactRef toDomain(ContactRefJpaEntity entity) {
        return new ContactRef(entity.getId(), entity.getCompanyId(), entity.getFullName(), entity.getEmail(),
                entity.isActive(), entity.isArchived());
    }

    private static ProductRef toDomain(ProductRefJpaEntity entity) {
        return new ProductRef(entity.getId(), entity.getSku(), entity.getName(), entity.getCategory(), entity.getBilling(),
                entity.getUnitPrice(), entity.getMaxDiscountPercent(), entity.isActive(), entity.isArchived());
    }
}
