package com.TMP.tms.dto.taskdto;

import com.TMP.tms.common.TaskPriority;
import com.TMP.tms.common.TaskStatus;

import java.util.UUID;

// Standard Task Response
public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        UUID assigneeUserId,
        UUID projectId
) {
}
