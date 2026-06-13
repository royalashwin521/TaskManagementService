package com.TMP.tms.repository;

import com.TMP.tms.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    // Basic CRUD is automatically provided by JpaRepository
}