package com.pb.crm.agent;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
