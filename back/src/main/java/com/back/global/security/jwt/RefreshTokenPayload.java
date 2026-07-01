package com.back.global.security.jwt;

public record RefreshTokenPayload(
        Long userId,
        String jti
) {
}