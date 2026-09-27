package com.pb.crm.team.infrastructure.persistence;

import com.pb.crm.commons.audit.AuditRevisions;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.ResourceNotFoundException;
import com.pb.crm.team.domain.salesrep.SalesRep;
import com.pb.crm.team.domain.salesrep.SalesRepCriteria;
import com.pb.crm.team.domain.salesrep.SalesRepRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SalesRepRepositoryAdapter implements SalesRepRepository {

    private final SpringDataSalesRepRepository jpaRepository;
    private final SalesRepMapper mapper;

    public SalesRepRepositoryAdapter(SpringDataSalesRepRepository jpaRepository, SalesRepMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public SalesRep save(SalesRep salesRep) {
        SalesRepJpaEntity entity = salesRep.isNew()
                ? new SalesRepJpaEntity()
                : jpaRepository.findById(salesRep.getId())
                        .orElseThrow(() -> ResourceNotFoundException.forId("Vendedor", salesRep.getId()));
        mapper.copyToEntity(salesRep, entity);
        return mapper.toDomain(jpaRepository.saveAndFlush(entity));
    }

    @Override
    public Optional<SalesRep> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<SalesRep> search(SalesRepCriteria criteria, PageQuery page) {
        PageRequest pageable = PageRequest.of(page.page(), page.size(), Sort.by("name").ascending().and(Sort.by("id")));
        Page<SalesRepJpaEntity> result = jpaRepository.findAll(SalesRepSpecifications.matching(criteria), pageable);
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmailIgnoreCase(email.trim());
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return jpaRepository.existsByEmailIgnoreCaseAndIdNot(email.trim(), id);
    }

    @Override
    public boolean hasActiveSubordinates(Long managerId) {
        return jpaRepository.existsByManagerIdAndActiveTrueAndArchivedFalse(managerId);
    }

    @Override
    public List<AuditRevision<SalesRep>> findRevisions(Long id) {
        return jpaRepository.findRevisions(id).stream()
                .map(revision -> AuditRevisions.from(revision, mapper::toDomain))
                .toList();
    }
}
