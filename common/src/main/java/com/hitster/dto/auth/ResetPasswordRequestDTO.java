package com.hitster.dto.auth;

public record ResetPasswordRequestDTO(
        String email,
        String code,
        String newPassword) {
}