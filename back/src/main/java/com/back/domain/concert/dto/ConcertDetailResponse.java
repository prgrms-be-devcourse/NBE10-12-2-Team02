package com.back.domain.concert.dto;

import com.back.domain.concert.entity.Concert;

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
        boolean isValid
) {
    public static ConcertDetailResponse of(
            Concert concert,
            String venueName,
            String location,
            List<String> detailUrlList,
            Map<String, Integer> prices,
            boolean isValid
    ) {
        return new ConcertDetailResponse(
                concert.getConcertId(),
                concert.getConcertName(),
                concert.getDescription(),
                venueName,
                location,
                detailUrlList,
                prices,
                isValid
        );
    }
}