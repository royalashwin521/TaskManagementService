package com.TMP.uas.dto;

import com.TMP.uas.common.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Must be a valid email format")
    String email,

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    String password,

    // In a strict production system, you usually force this to USER.
    // For admin creation, we allow it to be passed.
    Role role 
) {}