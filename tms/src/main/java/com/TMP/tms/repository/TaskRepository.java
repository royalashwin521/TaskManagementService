package com.TMP.tms.repository;

import com.TMP.tms.common.TaskStatus;
import com.TMP.tms.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByAssigneeUserId(UUID assigneeUserId);
    List<Task> findByProjectId(UUID projectId);
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
}