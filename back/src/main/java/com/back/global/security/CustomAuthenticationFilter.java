package com.back.global.security;

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
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${custom.jwt.accessToken.secret}")
    private String accessTokenSecret;
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

        String authorization = rq.getHeader("Authorization", "");

        if (authorization.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = getAccessToken(authorization);

        Map<String, Object> parsedPayload = Ut.jwt.payload(accessTokenSecret, accessToken);

        if (parsedPayload == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Long id = ((Number) parsedPayload.get("id")).longValue();
        String name = (String) parsedPayload.get("name");

        User user = User.create(id, name);

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

    private String getAccessToken(String authorization) {
        if (!authorization.startsWith("Bearer ")) {
            throw new ServiceException(ErrorCode.AUTH_INVALID_BEARER_HEADER);
        }

        String[] accessTokenBits = authorization.split(" ", 2);
        String accessToken = accessTokenBits.length == 2 ? accessTokenBits[1] : "";

        if (accessToken.isBlank()) {
            throw new ServiceException(ErrorCode.AUTH_INVALID_BEARER_HEADER);
        }

        return accessToken;
    }


}
