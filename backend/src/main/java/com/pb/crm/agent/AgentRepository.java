package com.pb.crm.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, Long>, RevisionRepository<Agent, Long, Integer> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    List<Agent> findAllByOrderByNameAsc();

    List<Agent> findByActiveTrueOrderByNameAsc();

    List<Agent> findByDepartmentIgnoreCaseOrderByNameAsc(String department);

    long countByActiveTrue();
}
