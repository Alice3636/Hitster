package com.hitster.dto.admin;

import java.util.List;

public record DeleteUsersRequestDTO(
        List<Long> userIds
) {}