package com.pb.crm.ticket;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>,
        JpaSpecificationExecutor<Ticket>,
        RevisionRepository<Ticket, Long, Integer> {

    @EntityGraph(attributePaths = {"customer", "agent"})
    List<Ticket> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"customer", "agent"})
    List<Ticket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    @EntityGraph(attributePaths = {"customer", "agent"})
    List<Ticket> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);

    @EntityGraph(attributePaths = {"customer", "agent"})
    List<Ticket> findByStatusAndCustomer_IdOrderByCreatedAtDesc(TicketStatus status, Long customerId);

    @EntityGraph(attributePaths = {"customer", "agent", "interactions"})
    Optional<Ticket> findWithDetailsById(Long id);

    boolean existsByCustomer_Id(Long customerId);

    boolean existsByAgent_Id(Long agentId);

    long countByStatus(TicketStatus status);

    long countByAgent_IdAndStatusIn(Long agentId, List<TicketStatus> statuses);

    List<Ticket> findByStatusAndResolvedAtBefore(TicketStatus status, Instant threshold);

    @Query("select t.status as status, count(t) as total from Ticket t group by t.status")
    List<TicketStatusCount> countGroupedByStatus();

    @Query("""
            select t from Ticket t
            join fetch t.customer c
            left join fetch t.agent
            where c.id = :customerId
            order by t.createdAt desc
            """)
    List<Ticket> findByCustomerWithRelations(@Param("customerId") Long customerId);
}
