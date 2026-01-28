package com.myapp.reservations.dto.userdto;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String phone,
        Set<String> roles,
        String avatarUrl
) {}

