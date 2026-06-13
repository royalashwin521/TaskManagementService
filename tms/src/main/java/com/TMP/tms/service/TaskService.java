package com.TMP.tms.service;

import com.TMP.tms.common.TaskStatus;
import com.TMP.tms.dto.taskdto.*;
import com.TMP.tms.entity.Project;
import com.TMP.tms.entity.Task;
import com.TMP.tms.exception.BusinessException;
import com.TMP.tms.repository.ProjectRepository;
import com.TMP.tms.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;
    private final AuthServiceClient authServiceClient;

    @Transactional
    public TaskResponse createTask(TaskRequest request, String authHeader) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new BusinessException("Project not found with ID: " + request.projectId()));

        if (request.assigneeUserId() != null) {
            authServiceClient.validateUserExists(request.assigneeUserId(), authHeader);
        }

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setProject(project);
        task.setStatus(TaskStatus.TODO);

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(UUID taskId, TaskUpdateRequest request) {
        Task task = getTaskEntityById(taskId);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }


    /**
     * Updates the user assigned to a task.
     * This requires making an HTTP call to the Auth Service to ensure the new user exists.
     */
    @Transactional
    public TaskResponse updateTaskAssignee(UUID taskId, TaskAssigneeUpdateRequest request, String authHeader) {
        Task task = getTaskEntityById(taskId);

        if (request.assigneeUserId() != null) {
            authServiceClient.validateUserExists(request.assigneeUserId(), authHeader);
        }

        task.setAssigneeUserId(request.assigneeUserId());

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    /**
     * Transitions a task to a new status.
     * This is purely local to the taskdb and does not require Auth Service validation.
     */
    @Transactional
    public TaskResponse updateTaskStatus(UUID taskId, TaskStatusUpdateRequest request) {
        Task task = getTaskEntityById(taskId);

        validateTransition(task.getStatus(),request.status());

        task.setStatus(request.status());

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    public Page<TaskResponse> getAllTasks(String status, Pageable pageable) {
        Page<Task> tasks;
        if (status != null && !status.trim().isEmpty()) {
            tasks = taskRepository.findByStatus(TaskStatus.valueOf(status.toUpperCase()), pageable);
        } else {
            tasks = taskRepository.findAll(pageable);
        }

        return tasks.map(taskMapper::toResponse);
    }

    public TaskResponse getTaskById(UUID id) {
        Task task = getTaskEntityById(id);
        return taskMapper.toResponse(task);
    }

    public List<TaskResponse> getTasksByProject(UUID projectId) {
        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByAssignee(UUID assigneeUserId) {
        return taskRepository.findByAssigneeUserId(assigneeUserId)
                .stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteTask(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new BusinessException("Task not found with ID: " + id);
        }
        taskRepository.deleteById(id);
    }

    // --- Private Helper Methods ---

    /**
     * Helper to fetch the raw entity.
     */
    protected Task getTaskEntityById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Task not found with ID: " + id));
    }

    /**
     * Enforces the strict state machine requirements.
     */
    private void validateTransition(TaskStatus current, TaskStatus next) {
        boolean valid = switch (current) {
            case TODO -> next == TaskStatus.IN_PROGRESS;
            case IN_PROGRESS -> next == TaskStatus.DONE;
            case DONE -> next == TaskStatus.IN_PROGRESS; // Re-opening
        };

        if (!valid) {
            throw new BusinessException("Invalid task status transition from " + current + " to " + next);
        }
    }

}