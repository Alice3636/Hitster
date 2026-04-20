package com.hitster.dto;

public record ErrorResponseDTO(
    String errorCode,
    String errorMessage
) {}
