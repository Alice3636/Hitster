package com.hitster.dto.user;

public record UpdateProfileRequestDTO(
    String username,
    String email,
    String profilePicturePath
) {}
