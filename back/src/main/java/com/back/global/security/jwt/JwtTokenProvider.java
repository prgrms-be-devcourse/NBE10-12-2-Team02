package com.back.global.security.jwt;

import com.back.domain.user.entity.User;
import com.back.global.exception.ErrorCode;
import com.back.global.exception.ServiceException;
import com.back.global.security.jwt.payload.AccessTokenPayload;
import com.back.global.security.jwt.payload.RefreshTokenPayload;
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

    public RefreshTokenPayload parseRefreshToken(String refreshToken) {
        Map<String, Object> payload = Ut.jwt.payload(refreshTokenSecret, refreshToken);

        if (payload == null) {
            return null;
        }

        Long userId = Long.valueOf(payload.get("id").toString());
        String jti = payload.get("jti").toString();

        return new RefreshTokenPayload(userId, jti);
    }

    public AccessTokenPayload parseAccessToken(String accessToken) {
        Map<String, Object> payload = Ut.jwt.payload(accessTokenSecret, accessToken);

        if (payload == null) {
            throw new ServiceException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        Long userId = Long.valueOf(payload.get("id").toString());
        String name = payload.get("name").toString();

        return new AccessTokenPayload(userId, name);
    }

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
