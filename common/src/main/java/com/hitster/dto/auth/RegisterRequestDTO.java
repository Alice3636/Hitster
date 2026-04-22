package com.hitster.dto.auth;

public record RegisterRequestDTO(
    String username,
    String email,
    String password,
    String picturePath
) {}
