package com.hitster.dto.auth;

public record LoginResponseDTO(
    Long userId,
    String username,
    boolean isAdmin,
    String token
) {}
