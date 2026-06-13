package com.TMP.uas.dto;

import com.TMP.uas.common.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    Role role,
    LocalDateTime createdAt
) {}