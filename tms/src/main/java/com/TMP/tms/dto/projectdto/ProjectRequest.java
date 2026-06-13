package com.TMP.tms.dto.projectdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ProjectRequest(
    @NotBlank(message = "Project name is mandatory")
    String name,
    
    String description
) {}