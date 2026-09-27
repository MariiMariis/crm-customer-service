package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.commons.audit.AuditRevisions;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.ResourceNotFoundException;
import com.pb.crm.sales.domain.opportunity.Opportunity;
import com.pb.crm.sales.domain.opportunity.OpportunityCriteria;
import com.pb.crm.sales.domain.opportunity.OpportunityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OpportunityRepositoryAdapter implements OpportunityRepository {

    private static final Sort DEFAULT_SORT = Sort.by("expectedCloseDate").ascending()
            .and(Sort.by(Sort.Direction.DESC, "amount"))
            .and(Sort.by("id"));

    private final SpringDataOpportunityRepository jpaRepository;
    private final OpportunityMapper mapper;

    public OpportunityRepositoryAdapter(SpringDataOpportunityRepository jpaRepository, OpportunityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Opportunity save(Opportunity opportunity) {
        OpportunityJpaEntity entity = opportunity.isNew()
                ? new OpportunityJpaEntity()
                : jpaRepository.findById(opportunity.getId())
                        .orElseThrow(() -> ResourceNotFoundException.forId("Oportunidade", opportunity.getId()));
        mapper.copyToEntity(opportunity, entity);
        OpportunityJpaEntity saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Opportunity> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Opportunity> search(OpportunityCriteria criteria, PageQuery page) {
        Page<OpportunityJpaEntity> result = jpaRepository.findAll(
                OpportunitySpecifications.matching(criteria), PageRequest.of(page.page(), page.size(), DEFAULT_SORT));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
    }

    @Override
    public List<Opportunity> findAllMatching(OpportunityCriteria criteria) {
        return jpaRepository.findAll(OpportunitySpecifications.matching(criteria), DEFAULT_SORT).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditRevision<Opportunity>> findRevisions(Long id) {
        return jpaRepository.findRevisions(id).stream()
                .map(revision -> AuditRevisions.from(revision, mapper::toRevisionSnapshot))
                .toList();
    }
}
