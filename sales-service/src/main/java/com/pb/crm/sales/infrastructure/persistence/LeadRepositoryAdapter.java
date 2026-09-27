package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.commons.audit.AuditRevisions;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.ResourceNotFoundException;
import com.pb.crm.sales.domain.lead.Lead;
import com.pb.crm.sales.domain.lead.LeadCriteria;
import com.pb.crm.sales.domain.lead.LeadRepository;
import com.pb.crm.sales.domain.lead.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class LeadRepositoryAdapter implements LeadRepository {

    private final SpringDataLeadRepository jpaRepository;
    private final LeadMapper mapper;

    public LeadRepositoryAdapter(SpringDataLeadRepository jpaRepository, LeadMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Lead save(Lead lead) {
        LeadJpaEntity entity = lead.isNew()
                ? new LeadJpaEntity()
                : jpaRepository.findById(lead.getId())
                        .orElseThrow(() -> ResourceNotFoundException.forId("Lead", lead.getId()));
        mapper.copyToEntity(lead, entity);
        return mapper.toDomain(jpaRepository.saveAndFlush(entity));
    }

    @Override
    public Optional<Lead> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Lead> search(LeadCriteria criteria, PageQuery page) {
        Sort sort = Sort.by(Sort.Direction.DESC, "score").and(Sort.by(Sort.Direction.DESC, "createdAt")).and(Sort.by("id"));
        Page<LeadJpaEntity> result = jpaRepository.findAll(
                LeadSpecifications.matching(criteria), PageRequest.of(page.page(), page.size(), sort));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
    }

    @Override
    public boolean existsOpenWithEmail(String email, Long excludedId) {
        return jpaRepository.existsWithEmailInStatuses(email.trim(), LeadStatus.OPEN, excludedId);
    }

    @Override
    public Map<LeadStatus, Long> countByStatus() {
        Map<LeadStatus, Long> counts = new EnumMap<>(LeadStatus.class);
        for (LeadStatus status : LeadStatus.values()) {
            counts.put(status, 0L);
        }
        for (Object[] row : jpaRepository.countGroupedByStatus()) {
            counts.put((LeadStatus) row[0], (Long) row[1]);
        }
        return counts;
    }

    @Override
    public List<Lead> findConvertingRequestedBefore(Instant requestedBefore) {
        return jpaRepository.findByStatusAndConversionRequestedAtBefore(LeadStatus.CONVERTING, requestedBefore).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditRevision<Lead>> findRevisions(Long id) {
        return jpaRepository.findRevisions(id).stream()
                .map(revision -> AuditRevisions.from(revision, mapper::toDomain))
                .toList();
    }
}
