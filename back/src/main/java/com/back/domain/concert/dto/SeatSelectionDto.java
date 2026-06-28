package com.back.domain.concert.dto;

import com.back.domain.schedule.entity.SeatStatus;

import java.util.List;
import java.util.Map;

public record SeatSelectionDto(
        Long concertId,
        Long scheduleId,
        Map<String, Integer> prices,
        List<SeatDetailDto> seats
) {
    public record SeatDetailDto(
            String seatNumber,
            SeatStatus seatStatus,
            String gradeName
    ) {}
}
