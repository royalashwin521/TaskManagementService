package com.TMP.tms.dto.taskdto;

import com.TMP.tms.common.TaskStatus;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record TaskStatusUpdateRequest(
        TaskStatus status) {
}
