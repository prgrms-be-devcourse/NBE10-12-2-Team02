package com.back.domain.concert.dto;

import com.back.domain.concert.entity.Concert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ConcertDetailResponse(
        Long concertId,
        String concertName,
        String description,
        String venueName,
        String location,
        List<String> detailUrlList,
        Map<String, Integer> prices,
        String status
) {
    public static ConcertDetailResponse of(
            Concert concert,
            String venueName,
            String location,
            List<String> detailUrlList,
            Map<String, Integer> prices
    ) {
        String status = concert.getEndDate().isAfter(LocalDateTime.now()) ? "AVAILABLE" : "CLOSED";

        return new ConcertDetailResponse(
                concert.getConcertId(),
                concert.getConcertName(),
                concert.getDescription(),
                venueName,
                location,
                detailUrlList,
                prices,
                status
        );
    }
}