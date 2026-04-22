package com.hitster.dto.admin;

import java.util.List;

public record UsersResponseDTO(
        List<UserEntryDTO> users
) {}
