package com.back.global.security;

import com.back.domain.auth.service.AuthTokenService;
import com.back.domain.user.entity.User;
import com.back.global.exception.ErrorCode;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import com.back.standard.util.Ut;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final AuthTokenService authTokenService;
    private final Rq rq;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            work(request, response, filterChain);
        } catch (ServiceException e) {
            SecurityContextHolder.clearContext();

            RsData<Void> rsData = e.getRsData();
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(rsData.statusCode());
            response.getWriter().write(
                    Ut.json.toString(
                            rsData,
                            "{\"resultCode\":\"500-1\",\"msg\":\"JSON 변환 실패\",\"data\":null}"
                    )
            );
        }
    }

    private void work(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = rq.getCookieValue("refreshToken", "");
        String authorization = rq.getHeader("Authorization", "");

        boolean isRefreshTokenExists = !refreshToken.isBlank();
        boolean isAuthorizationExists = !authorization.isBlank();

        if (!isRefreshTokenExists && !isAuthorizationExists) {
            throw new ServiceException(ErrorCode.AUTH_LOGIN_REQUIRED);
        }

        if (isAuthorizationExists && !authorization.startsWith("Bearer "))
            throw new ServiceException(ErrorCode.AUTH_INVALID_BEARER_HEADER);

        String accessToken = "";
        if (isAuthorizationExists) {
            String[] accessTokenBits = authorization.split(" ", 2);
            accessToken = accessTokenBits.length == 2 ? accessTokenBits[1] : "";
        }

        boolean isAccessTokenExists = !accessToken.isBlank();

        User user = null;

        if (isAccessTokenExists) {
            Map<String, Object> payload = authTokenService.payload(accessToken);

            if (payload != null) {
                Number idNumber = (Number) payload.get("id");
                Long id = idNumber.longValue();
                String name = (String) payload.get("name");

                user = new User(id, name);
            }
        }

        boolean isMemberLoadedFromRefreshToken = false;

        if (user == null && isRefreshTokenExists) {
            user = authTokenService.findUser(refreshToken);
            isMemberLoadedFromRefreshToken = user != null;
        }

        if (user == null) {
            throw new ServiceException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        if (isMemberLoadedFromRefreshToken) {
            AuthTokenService.TokenResponse tokenResponse = authTokenService.refresh(refreshToken);

            String actorAccessToken = "Bearer " + tokenResponse.accessToken();
            String actorRefreshToken = tokenResponse.refreshToken();

            rq.setHeader("Authorization", actorAccessToken);
            rq.setCookie("refreshToken", actorRefreshToken);
        }

        UserDetails securityUser = new SecurityUser(
                user.getUserId(),
                user.getName()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                securityUser,
                null,
                securityUser.getAuthorities()
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
