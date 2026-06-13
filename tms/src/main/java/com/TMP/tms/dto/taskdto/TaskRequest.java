package com.TMP.tms.dto.taskdto;

import com.TMP.tms.common.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

// Request to create a new Task
public record TaskRequest(
    @NotBlank(message = "Title is mandatory") String title,
    String description,
    @NotNull TaskPriority priority,
    @NotNull(message = "ProjectId is mandatory") UUID projectId,
    UUID assigneeUserId
) {}

