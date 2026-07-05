package com.back.global.security.interceptor;

import com.back.global.exception.ErrorCode;
import com.back.global.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueueInterceptor implements HandlerInterceptor {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("X-Queue-Token");

        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        String scheduleIdStr = (pathVariables != null && pathVariables.containsKey("scheduleId"))
                ? pathVariables.get("scheduleId")
                : request.getHeader("X-Schedule-Id");

        if (scheduleIdStr == null || scheduleIdStr.isBlank()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST);
        }

        Long scheduleId = Long.parseLong(scheduleIdStr);
        String activeQueueKey = "queue:active:schedule:" + scheduleId;
        Double score = redisTemplate.opsForZSet().score(activeQueueKey, token);

        if (score == null || score < System.currentTimeMillis()) {
            throw new ServiceException(ErrorCode.QUEUE_SESSION_EXPIRED);
        }
        return true;
    }
}
