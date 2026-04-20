package com.hitster.dto.admin;

public record AdminUserDTO(
    Long id,
    String username,
    String email,
    boolean isAdmin
) {}