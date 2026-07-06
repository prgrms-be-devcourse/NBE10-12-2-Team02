package com.back.domain.waiting.dto;

public record WaitingQueueRegisterResponse(
        Long scheduleId,
        Long userId,
        Long rank,
        boolean registered
) {
}
