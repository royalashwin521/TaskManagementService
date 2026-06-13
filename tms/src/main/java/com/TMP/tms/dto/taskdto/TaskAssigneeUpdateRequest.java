package com.TMP.tms.dto.taskdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TaskAssigneeUpdateRequest(
        @NotNull(message = "ProjectId is mandatory") UUID assigneeUserId) {
}
