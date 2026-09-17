package com.pb.crm.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TicketStatusHistoryRepository extends JpaRepository<TicketStatusHistory, Long> {

    List<TicketStatusHistory> findByTicket_IdOrderByChangedAtAscIdAsc(Long ticketId);

    List<TicketStatusHistory> findByToStatusAndChangedAtBetween(TicketStatus toStatus, Instant from, Instant to);

    long countByTicket_Id(Long ticketId);

    @Query("""
            select h from TicketStatusHistory h
            join fetch h.ticket t
            where h.changedBy = :actor
            order by h.changedAt desc
            """)
    List<TicketStatusHistory> findByActor(@Param("actor") String actor);
}
