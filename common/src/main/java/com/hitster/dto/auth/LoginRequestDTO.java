package com.hitster.dto.auth;

public record LoginRequestDTO(
    String email,
    String password
) {}
