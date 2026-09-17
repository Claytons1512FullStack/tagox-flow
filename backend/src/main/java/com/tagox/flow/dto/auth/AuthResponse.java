package com.tagox.flow.dto.auth;

public record AuthResponse(
        String token,
        String tokenType
) {
}