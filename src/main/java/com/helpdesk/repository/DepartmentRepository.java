package com.helpdesk.repository;

import com.helpdesk.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Department entity database operations.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /** Find department by name for uniqueness validation */
    Optional<Department> findByName(String name);

    /** Check if a department name already exists */
    boolean existsByName(String name);
}
