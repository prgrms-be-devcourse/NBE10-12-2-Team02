package com.back.domain.waiting.service;

import com.back.domain.concert.service.ConcertService;

import com.back.domain.user.repository.UserRepository;
import com.back.domain.waiting.WaitingQueueManager;
import com.back.domain.waiting.dto.WaitingQueueRegisterResponse;
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

    public WaitingQueueRegisterResponse registerWaiting(Long concertId, Long scheduleId, Long userId) {

        userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        concertService.validateConcertScheduleMatch(concertId, scheduleId);
        return waitingQueueManager.registerWaiting(concertId, scheduleId, userId);
    }
}
