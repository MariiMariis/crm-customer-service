package com.pb.crm.accounts.infrastructure.persistence;

import com.pb.crm.accounts.domain.company.Cnpj;
import com.pb.crm.accounts.domain.company.Company;
import com.pb.crm.accounts.domain.company.CompanyCriteria;
import com.pb.crm.accounts.domain.company.CompanyRepository;
import com.pb.crm.commons.audit.AuditRevisions;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class CompanyRepositoryAdapter implements CompanyRepository {

    private final SpringDataCompanyRepository jpaRepository;
    private final AccountsMapper mapper;

    public CompanyRepositoryAdapter(SpringDataCompanyRepository jpaRepository, AccountsMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Company save(Company company) {
        CompanyJpaEntity entity = company.isNew()
                ? new CompanyJpaEntity()
                : jpaRepository.findById(company.getId())
                        .orElseThrow(() -> ResourceNotFoundException.forId("Empresa", company.getId()));
        mapper.copyToEntity(company, entity);
        return mapper.toDomain(jpaRepository.saveAndFlush(entity));
    }

    @Override
    public Optional<Company> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Company> findByCnpj(Cnpj cnpj) {
        return jpaRepository.findByCnpj(cnpj.value()).map(mapper::toDomain);
    }

    @Override
    public Map<Long, Company> findAllByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        return jpaRepository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toMap(Company::getId, Function.identity()));
    }

    @Override
    public PageResult<Company> search(CompanyCriteria criteria, PageQuery page) {
        PageRequest pageable = PageRequest.of(page.page(), page.size(), Sort.by("legalName").ascending().and(Sort.by("id")));
        Page<CompanyJpaEntity> result = jpaRepository.findAll(AccountsSpecifications.companies(criteria), pageable);
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
    }

    @Override
    public boolean existsByCnpj(Cnpj cnpj) {
        return jpaRepository.existsByCnpj(cnpj.value());
    }

    @Override
    public boolean existsByCnpjAndIdNot(Cnpj cnpj, Long id) {
        return jpaRepository.existsByCnpjAndIdNot(cnpj.value(), id);
    }

    @Override
    public List<AuditRevision<Company>> findRevisions(Long id) {
        return jpaRepository.findRevisions(id).stream()
                .map(revision -> AuditRevisions.from(revision, mapper::toDomain))
                .toList();
    }
}
