package com.back.domain.concert.controller;

import com.back.domain.concert.dto.SeatOccupyRequest;
import com.back.domain.concert.dto.SeatOccupyResponse;
import com.back.domain.concert.dto.SeatSelectionResponse;
import com.back.domain.concert.service.ConcertService;
import com.back.global.annotation.ApiV1;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@ApiV1
@RestController
@RequestMapping("/concerts")
@RequiredArgsConstructor
@Tag(name = "Concert", description = "Concert API")
public class ConcertController {
    private final ConcertService concertService;

    @GetMapping("/{concertId}/schedules/{scheduleId}/seats")
    public RsData<SeatSelectionResponse> getSeatSelection(
            @PathVariable Long concertId,
            @PathVariable Long scheduleId) {

        SeatSelectionResponse response = concertService.getSeatSelection(concertId, scheduleId);

        return new RsData<>(
                "200-1",
                "좌석 선택 페이지 조회 성공",
                response
        );
    }

    @PostMapping("/{concertId}/schedules/{scheduleId}/seats/occupy")
    public RsData<SeatOccupyResponse> seatOccupy(
            @PathVariable Long concertId,
            @PathVariable Long scheduleId,
            @RequestBody SeatOccupyRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        SeatOccupyResponse response = concertService.seatOccupy(concertId, scheduleId, request.seatNumber(), userId);

        return new RsData<>(
                "200-1",
                "좌석 임시 선점에 성공했습니다.",
                response
        );
    }
}
