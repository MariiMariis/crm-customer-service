package com.pb.crm.team.application.salesrep;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.commons.error.ResourceNotFoundException;
import com.pb.crm.commons.messaging.DomainEventPublisher;
import com.pb.crm.team.application.salesrep.dto.SalesRepRequest;
import com.pb.crm.team.application.salesrep.dto.SalesRepResponse;
import com.pb.crm.team.domain.salesrep.SalesRep;
import com.pb.crm.team.domain.salesrep.SalesRepCriteria;
import com.pb.crm.team.domain.salesrep.SalesRepRepository;
import com.pb.crm.team.domain.salesrep.SalesRole;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SalesRepServiceImpl implements SalesRepService {

    private static final String ENTITY_NAME = "Vendedor";
    private static final String EVENT_PREFIX = "team.";
    private static final String AGGREGATE_TYPE = "SalesRep";

    private final SalesRepRepository repository;
    private final DomainEventPublisher eventPublisher;

    public SalesRepServiceImpl(SalesRepRepository repository, DomainEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public SalesRepResponse create(SalesRepRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new BusinessRuleException("ja existe um vendedor cadastrado com este email");
        }
        SalesRep salesRep = SalesRep.register(
                request.name(),
                request.email(),
                request.phone(),
                request.team(),
                request.role(),
                request.monthlyQuota(),
                resolveManager(request.managerId())
        );
        return toResponse(persist(salesRep));
    }

    @Override
    @Transactional
    public SalesRepResponse update(Long id, SalesRepRequest request) {
        SalesRep salesRep = load(id);
        assertVersion(salesRep, request.version());
        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new BusinessRuleException("ja existe outro vendedor cadastrado com este email");
        }
        if (salesRep.isManager() && request.role() == SalesRole.REP && repository.hasActiveSubordinates(id)) {
            throw new BusinessRuleException("o gestor possui vendedores ativos vinculados; reatribua-os antes de alterar o papel");
        }
        salesRep.update(
                request.name(),
                request.email(),
                request.phone(),
                request.team(),
                request.role(),
                request.monthlyQuota(),
                resolveManager(request.managerId())
        );
        return toResponse(persist(salesRep));
    }

    @Override
    @Transactional(readOnly = true)
    public SalesRepResponse findById(Long id) {
        return toResponse(load(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<SalesRepResponse> search(SalesRepCriteria criteria, PageQuery page) {
        PageResult<SalesRep> result = repository.search(criteria, page);
        Map<Long, String> managerNames = loadManagerNames(result.content());
        return result.map(salesRep -> SalesRepResponse.from(salesRep, managerNames.get(salesRep.getManagerId())));
    }

    @Override
    @Transactional
    public SalesRepResponse activate(Long id) {
        SalesRep salesRep = load(id);
        salesRep.activate();
        return toResponse(persist(salesRep));
    }

    @Override
    @Transactional
    public SalesRepResponse deactivate(Long id) {
        SalesRep salesRep = load(id);
        assertNoActiveSubordinates(salesRep);
        salesRep.deactivate();
        return toResponse(persist(salesRep));
    }

    @Override
    @Transactional
    public SalesRepResponse archive(Long id) {
        SalesRep salesRep = load(id);
        assertNoActiveSubordinates(salesRep);
        salesRep.archive();
        return toResponse(persist(salesRep));
    }

    @Override
    @Transactional
    public SalesRepResponse restore(Long id) {
        SalesRep salesRep = load(id);
        salesRep.restore();
        return toResponse(persist(salesRep));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditRevision<SalesRepResponse>> findRevisions(Long id) {
        load(id);
        return repository.findRevisions(id).stream()
                .map(revision -> revision.map(SalesRepResponse::from))
                .toList();
    }

    private SalesRep persist(SalesRep salesRep) {
        List<String> events = salesRep.pullEvents();
        SalesRep saved = repository.save(salesRep);
        SalesRepSnapshot snapshot = SalesRepSnapshot.from(saved);
        events.forEach(event -> eventPublisher.publish(EVENT_PREFIX + event, AGGREGATE_TYPE, saved.getId(), snapshot));
        return saved;
    }

    private SalesRep load(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.forId(ENTITY_NAME, id));
    }

    private SalesRep resolveManager(Long managerId) {
        if (managerId == null) {
            return null;
        }
        return repository.findById(managerId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Gestor", managerId));
    }

    private void assertNoActiveSubordinates(SalesRep salesRep) {
        if (salesRep.isManager() && repository.hasActiveSubordinates(salesRep.getId())) {
            throw new BusinessRuleException("o gestor possui vendedores ativos vinculados; reatribua-os antes de continuar");
        }
    }

    private static void assertVersion(SalesRep salesRep, Long expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(salesRep.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(SalesRep.class, salesRep.getId());
        }
    }

    private SalesRepResponse toResponse(SalesRep salesRep) {
        String managerName = salesRep.getManagerId() == null
                ? null
                : repository.findById(salesRep.getManagerId()).map(SalesRep::getName).orElse(null);
        return SalesRepResponse.from(salesRep, managerName);
    }

    private Map<Long, String> loadManagerNames(List<SalesRep> salesReps) {
        Set<Long> managerIds = salesReps.stream()
                .map(SalesRep::getManagerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> names = new HashMap<>();
        managerIds.forEach(managerId -> repository.findById(managerId)
                .ifPresent(manager -> names.put(managerId, manager.getName())));
        return names;
    }
}
