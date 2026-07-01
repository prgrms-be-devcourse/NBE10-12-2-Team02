package com.back.global.security.filter;

import com.back.global.exception.ErrorCode;
import com.back.global.exception.ServiceException;
import com.back.global.requestcontext.RequestContext;
import com.back.global.rsData.RsData;
import com.back.global.security.SecurityUser;
import com.back.global.security.jwt.JwtTokenProvider;
import com.back.global.security.jwt.payload.AccessTokenPayload;
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

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final RequestContext rq;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            authenticateByAccessToken();
            filterChain.doFilter(request, response);
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

    private void authenticateByAccessToken() {
        String authorization = rq.getHeader("Authorization", "");

        if (authorization.isBlank()) {
            return;
        }

        String accessToken = extractAccessToken(authorization);
        AccessTokenPayload payload = jwtTokenProvider.parseAccessToken(accessToken);

        Authentication authentication = createAuthentication(payload);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String extractAccessToken(String authorization) {
        if (!authorization.startsWith("Bearer ")) {
            throw new ServiceException(ErrorCode.AUTH_INVALID_BEARER_HEADER);
        }

        String accessToken = authorization.substring("Bearer ".length()).trim();

        if (accessToken.isBlank()) {
            throw new ServiceException(ErrorCode.AUTH_INVALID_BEARER_HEADER);
        }

        return accessToken;
    }

    private Authentication createAuthentication(AccessTokenPayload payload) {
        Long id = payload.userId();
        String name = payload.name();

        UserDetails securityUser = new SecurityUser(id, name);

        return new UsernamePasswordAuthenticationToken(
                securityUser,
                null,
                securityUser.getAuthorities()
        );
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        return "OPTIONS".equals(method)
                || ("POST".equals(method) && (
                path.matches("/api/[^/]+/auth/login")
                        || path.matches("/api/[^/]+/auth/refresh")
                        || path.matches("/api/[^/]+/auth/logout")
                        || path.matches("/api/[^/]+/users/signin")
        ));
    }
}
