package com.TMP.tms.repository;

import com.TMP.tms.common.TaskStatus;
import com.TMP.tms.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByAssigneeUserId(UUID assigneeUserId);
    List<Task> findByProjectId(UUID projectId);
    List<Task> findByStatus(TaskStatus status);
}