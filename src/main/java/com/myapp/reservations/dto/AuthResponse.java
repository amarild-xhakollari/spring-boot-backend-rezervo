package com.myapp.reservations.dto;

import com.myapp.reservations.dto.userdto.UserResponse;

public record AuthResponse(
        String token,
        UserResponse user
) {
}