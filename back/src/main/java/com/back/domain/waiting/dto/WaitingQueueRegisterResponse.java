package com.back.domain.waiting.dto;

public record WaitingQueueRegisterResponse(
        Long concertId,
        Long scheduleId,
        Long userId,
        Long rank,
        boolean registered
) {
}
