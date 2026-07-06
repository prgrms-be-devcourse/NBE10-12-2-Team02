package com.back.global.security.oauth2.loginhandler;

import com.back.domain.auth.dto.TokenResponse;
import com.back.domain.auth.service.AuthService;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ErrorCode;
import com.back.global.exception.ServiceException;
import com.back.global.requestcontext.RequestContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final RequestContext requestContext;

    private static final String FRONT_CALLBACK_URL = "http://localhost:3000";

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Long userId = Long.valueOf(oAuth2User.getAttribute("userId").toString());

        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        TokenResponse tokenResponse = authService.issueTokens(user);

        requestContext.setCookie("refreshToken", tokenResponse.refreshToken(), "/api/v1/auth");

        String redirectUrl = UriComponentsBuilder
                .fromUriString(FRONT_CALLBACK_URL)
                .fragment("accessToken=" + tokenResponse.accessToken())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}