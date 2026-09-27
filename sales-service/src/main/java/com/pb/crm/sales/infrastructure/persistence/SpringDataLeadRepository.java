package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.sales.domain.lead.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface SpringDataLeadRepository extends JpaRepository<LeadJpaEntity, Long>,
        JpaSpecificationExecutor<LeadJpaEntity>,
        RevisionRepository<LeadJpaEntity, Long, Integer> {

    @Query("""
            select count(l) > 0 from LeadJpaEntity l
            where lower(l.email) = lower(:email)
              and l.status in :statuses
              and l.archived = false
              and (:excludedId is null or l.id <> :excludedId)
            """)
    boolean existsWithEmailInStatuses(@Param("email") String email,
                                      @Param("statuses") Collection<LeadStatus> statuses,
                                      @Param("excludedId") Long excludedId);

    List<LeadJpaEntity> findByStatusAndConversionRequestedAtBefore(LeadStatus status, Instant requestedBefore);

    @Query("select l.status, count(l) from LeadJpaEntity l where l.archived = false group by l.status")
    List<Object[]> countGroupedByStatus();
}
