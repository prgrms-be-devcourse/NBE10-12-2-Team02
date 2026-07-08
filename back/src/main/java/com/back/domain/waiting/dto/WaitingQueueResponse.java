package com.back.domain.waiting.dto;

public record WaitingQueueResponse(
        Long concertId,
        Long scheduleId,
        Long userId,
        Long rank,
        Long myQueueNumber
) {
    public static WaitingQueueResponse of(Long concertId, Long scheduleId, Long userId, Long rank, Long myQueueNumber) {
        return new WaitingQueueResponse(concertId, scheduleId, userId, rank, myQueueNumber);
    }
}
