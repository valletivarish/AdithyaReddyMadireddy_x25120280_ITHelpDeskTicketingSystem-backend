package com.helpdesk.repository;

import com.helpdesk.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Agent entity database operations.
 */
@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    /** Find agents belonging to a specific department */
    List<Agent> findByDepartmentId(Long departmentId);

    /** Find all currently available agents */
    List<Agent> findByAvailableTrue();

    /** Find agent by linked user ID */
    Optional<Agent> findByUserId(Long userId);

    /** Count available agents for dashboard stats */
    long countByAvailableTrue();

    /** Check if a user already has an agent profile */
    boolean existsByUserId(Long userId);
}
