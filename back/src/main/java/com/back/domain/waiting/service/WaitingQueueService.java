package com.back.domain.waiting.service;

import com.back.domain.concert.service.ConcertService;
import com.back.domain.user.repository.UserRepository;
import com.back.domain.waiting.dto.WaitingQueueResponse;
import com.back.global.exception.ErrorCode;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WaitingQueueService {
    private final WaitingQueueManager waitingQueueManager;
    private final UserRepository userRepository;
    private final ConcertService concertService;

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

    private void validateUser(Long userId) {
        userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }
}
