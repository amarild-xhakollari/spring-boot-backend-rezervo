package com.myapp.reservations.dto;

public record SignInRequest(
        String identifier,
        String password
) {
}
