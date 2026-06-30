package com.back.global.security;

import com.back.domain.user.entity.User;
import com.back.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${custom.jwt.accessToken.secret}")
    private String accessTokenSecret;

    @Value("${custom.jwt.refreshToken.secret}")
    private String refreshTokenSecret;

    @Value("${custom.jwt.accessToken.expirationSeconds}")
    private int accessTokenExpireSeconds;

    @Value("${custom.jwt.refreshToken.expirationSeconds}")
    private int refreshTokenExpireSeconds;

    public String createAccessToken(User user) {
        return Ut.jwt.toString(
                accessTokenSecret,
                accessTokenExpireSeconds,
                Map.of(
                        "id", user.getUserId(),
                        "name", user.getName()
                )
        );
    }

    public String createRefreshToken(User user, String jti) {
        return Ut.jwt.toString(
                refreshTokenSecret,
                refreshTokenExpireSeconds,
                Map.of(
                        "id", user.getUserId(),
                        "name", user.getName(),
                        "jti", jti
                )
        );
    }
}
