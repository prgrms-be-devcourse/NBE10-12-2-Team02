package com.back.global.security;

public record RefreshTokenPayload(
        Long userId,
        String jti
) {
}