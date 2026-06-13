package com.TMP.tms.dto.taskdto;

import com.TMP.tms.common.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskUpdateRequest(
    @NotBlank(message = "Title is mandatory") String title,
    String description,
    @NotNull TaskPriority priority
) {}

