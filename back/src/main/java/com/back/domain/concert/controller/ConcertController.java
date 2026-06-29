package com.back.domain.concert.controller;

import com.back.domain.concert.dto.ConcertDetailResponse;
import com.back.domain.concert.dto.ConcertListResponse;
import com.back.domain.concert.dto.SeatSelectionResponse;
import com.back.domain.concert.service.ConcertService;
import com.back.global.annotation.ApiV1;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1
@RestController
@RequestMapping("/concerts")
@RequiredArgsConstructor
@Tag(name = "Concert", description = "Concert API")
public class ConcertController {
    private final ConcertService concertService;

    // 콘서트 목록 조회
    @GetMapping
    public RsData<List<ConcertListResponse>> getConcerts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "closingSoon") String sort) {

        List<ConcertListResponse> data = concertService.getConcerts(keyword, sort);
        return new RsData<>("200-1", "콘서트 목록 조회 성공", data);
    }

    // 콘서트 상세 조회
    @GetMapping("/{concertId}")
    public RsData<ConcertDetailResponse> getConcertDetail(
            @PathVariable Long concertId) {

        ConcertDetailResponse data = concertService.getConcertDetail(concertId);
        return new RsData<>("200-1", "콘서트 상세 정보 조회 성공", data);
    }

    // 좌석 선택 조회 (기존)
    @GetMapping("/{concertId}/schedules/{scheduleId}/seats")
    public RsData<SeatSelectionResponse> getSeatSelection(
            @PathVariable Long concertId,
            @PathVariable Long scheduleId) {

        SeatSelectionResponse data = concertService.getSeatSelection(concertId, scheduleId);
        return new RsData<>("200-1", "좌석 선택 페이지 조회 성공", data);
    }
}