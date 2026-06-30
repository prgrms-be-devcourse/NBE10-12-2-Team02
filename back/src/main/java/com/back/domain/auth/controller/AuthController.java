package com.back.domain.auth.controller;

import com.back.domain.auth.dto.LoginRequest;
import com.back.domain.auth.dto.LoginResponse;
import com.back.domain.auth.service.AuthTokenService;
import com.back.domain.user.entity.User;
import com.back.domain.user.service.UserService;
import com.back.global.annotation.ApiV1;
import com.back.global.exception.ErrorCode;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@ApiV1
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Auth API")
public class AuthController {
    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final Rq rq;

    @Value("${custom.jwt.accessToken.expirationSeconds}")
    private Number accessTokenExpirationSeconds;

    @PostMapping("/login")
    public RsData<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        User user = userService.findById(request.id())
                .orElseThrow(() -> new ServiceException(ErrorCode.AUTH_ID_NOT_FOUND));

        authTokenService.checkPassword(
                user,
                request.password()
        );

        AuthTokenService.TokenResponse tokenResponse = authTokenService.login(user);

        String accessToken = "Bearer " + tokenResponse.accessToken();
        String refreshToken = tokenResponse.refreshToken();

        rq.setCookie("refreshToken", refreshToken);
        rq.setHeader("Authorization", accessToken);

        return new RsData<>(
                "200-1",
                "로그인 성공 및 인증 토큰이 발급되었습니다.",
                LoginResponse.of(accessToken, "Bearer", accessTokenExpirationSeconds)
        );
    }
}
