package com.TMP.tms.transport;

import com.TMP.tms.dto.taskdto.*;
import com.TMP.tms.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest taskRequest,
                                                   @RequestHeader("Authorization") String authHeader) {
        TaskResponse createdTask = taskService.createTask(taskRequest, authHeader);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    /**
     * Endpoint: PATCH /api/v1/tasks/{id}/assignee
     * Description: Reassigns a task. Requires Auth Header to verify new assignee ID via bridge.
     */
    @PatchMapping("/{id}/assigne")
    public ResponseEntity<TaskResponse> updateTaskAssignee(
            @PathVariable UUID id,
            @Valid @RequestBody TaskAssigneeUpdateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        TaskResponse response = taskService.updateTaskAssignee(id, request, authHeader);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint: PATCH /api/v1/tasks/{id}/status
     * Description: Transitions a task to a new status.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TaskStatusUpdateRequest request) {

        TaskResponse response = taskService.updateTaskStatus(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint: PUT /api/v1/tasks/{id}
     * Description: Update task.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody TaskUpdateRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint: DELETE /api/v1/tasks/{id}
     * Description: DELETE task.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return  ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
            @RequestParam(required = false) String status, Pageable pageable) {
        return ResponseEntity.ok(taskService.getAllTasks(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResponse>> getTasksByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<List<TaskResponse>> getMyTasks(@RequestParam UUID userId) {
        return ResponseEntity.ok(taskService.getTasksByAssignee(userId));
    }
}