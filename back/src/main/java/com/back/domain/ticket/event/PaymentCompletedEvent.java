package com.back.domain.ticket.event;

public record PaymentCompletedEvent(
        Long scheduleId,
        Long userId
) {
}
