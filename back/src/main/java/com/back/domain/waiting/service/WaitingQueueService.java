package com.back.domain.waiting.service;

import com.back.domain.concert.service.ConcertService;
import com.back.domain.queue.event.EntryAllowedEvent;
import com.back.domain.user.repository.UserRepository;
import com.back.domain.waiting.dto.WaitingQueueResponse;
import com.back.global.exception.ErrorCode;
import com.back.global.exception.ServiceException;
import com.back.global.security.interceptor.QueueInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WaitingQueueService {
    //TODO 이거 환경변수로 제거
    private static final long ENTRY_TOKEN_TTL_MILLIS = 10 * 60 * 1000L;

    private final WaitingQueueManager waitingQueueManager;
    private final UserRepository userRepository;
    private final ConcertService concertService;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    public WaitingQueueResponse registerWaiting(Long concertId, Long scheduleId, Long userId) {
        validateUser(userId);
        concertService.validateConcertScheduleMatch(concertId, scheduleId);

        Long rank = waitingQueueManager.registerWaiting(scheduleId, userId);

        return WaitingQueueResponse.of(
                concertId,
                scheduleId,
                userId,
                rank
        );
    }

    public WaitingQueueResponse showWaitingRank(Long concertId, Long scheduleId, Long userId) {
        validateUser(userId);
        concertService.validateConcertScheduleMatch(concertId, scheduleId);

        Long rank = waitingQueueManager.showWaitingRank(scheduleId, userId);

        return WaitingQueueResponse.of(
                concertId,
                scheduleId,
                userId,
                rank
        );
    }

    public void cancelWaiting(Long concertId, Long scheduleId, Long userId) {
        validateUser(userId);
        concertService.validateConcertScheduleMatch(concertId, scheduleId);

        waitingQueueManager.cancelWaiting(scheduleId, userId);
    }

    public List<Long> allowEntry(Long concertId, Long scheduleId, int count) {
        concertService.validateConcertScheduleMatch(concertId, scheduleId);

        if (count <= 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST);
        }

        List<Long> userIds = waitingQueueManager.popUsers(scheduleId, count);

        for (Long userId : userIds) {
            String entryToken = UUID.randomUUID().toString();
            long expiredAt = System.currentTimeMillis() + ENTRY_TOKEN_TTL_MILLIS;

            redisTemplate.opsForZSet().add(
                    QueueInterceptor.generateQueueActiveKey(scheduleId),
                    entryToken,
                    expiredAt
            );

            eventPublisher.publishEvent(
                    new EntryAllowedEvent(scheduleId, userId, entryToken, expiredAt)
            );
        }

        return userIds;
    }


    private void validateUser(Long userId) {
        userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }
}
