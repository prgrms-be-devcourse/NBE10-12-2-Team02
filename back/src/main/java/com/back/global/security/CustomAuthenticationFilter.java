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
        // 임시 코드(필터 적용 x)
        if (request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 두개를 각각 불러옴
        String refreshToken = rq.getCookieValue("refreshToken", "");
        String authorization = rq.getHeader("Authorization", "");

        boolean isRefreshTokenExists = !refreshToken.isBlank();
        boolean isAuthorizationExists = !authorization.isBlank();

        if (!isRefreshTokenExists && !isAuthorizationExists) {
            throw new ServiceException(ErrorCode.AUTH_LOGIN_REQUIRED);
        }

        // Authorization 헤더가 존재하면 Bearer 형식이어야 함
        if (isAuthorizationExists && !authorization.startsWith("Bearer "))
            throw new ServiceException(ErrorCode.AUTH_INVALID_BEARER_HEADER);

        // Bearer accessToken을 두개로 나누어 [1](accessToken)을 accessToken 변수에 넣음
        String accessToken = "";
        if (isAuthorizationExists) {
            String[] accessTokenBits = authorization.split(" ", 2);
            accessToken = accessTokenBits.length == 2 ? accessTokenBits[1] : "";
        }

        // 토큰 두 개가 존재하는지 확인하는 변수
        boolean isAccessTokenExists = !accessToken.isBlank();

        // 멤버 -> 받아온 jwt에서 payload를 통해 멤버 정보를 저장
        // accessToken이 유효한지 확인 하는 변수 생성
        User user = null;

        // accessToken이 유효하다면 -> payload에서 멤버값 추출 후 변수에 저장
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

        // accessToken이 없거나 유효하지 않아 refreshToken으로 인증했다면 새 토큰을 응답에 내려준다.
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

        // 이 시점 이후부터는 시큐리티가 이 요청을 인증된 사용자의 요청이다.
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
