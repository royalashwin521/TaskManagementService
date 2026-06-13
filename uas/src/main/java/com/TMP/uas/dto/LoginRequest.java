package com.TMP.uas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Must be a valid email format")
    String email,

    @NotBlank(message = "Password is mandatory")
    String password
) {}