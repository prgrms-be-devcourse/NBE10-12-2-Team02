package com.back.domain.concert.service;

import com.back.domain.concert.dto.ConcertDetailResponse;
import com.back.domain.concert.dto.ConcertListResponse;
import com.back.domain.concert.dto.SeatSelectionResponse;
import com.back.domain.concert.dto.SeatSelectionResponse.SeatDetailResponse;
import com.back.domain.concert.entity.Concert;
import com.back.domain.concert.entity.ConcertDetail;
import com.back.domain.concert.repository.ConcertDeatilRepository;
import com.back.domain.concert.repository.ConcertRepository;
import com.back.domain.schedule.entity.Schedule;
import com.back.domain.schedule.entity.ScheduleSeat;
import com.back.domain.schedule.repository.ScheduleRepository;
import com.back.domain.schedule.repository.ScheduleSeatRepository;
import com.back.domain.venue.entity.Venue;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final ScheduleRepository scheduleRepository;
    private final ConcertRepository concertRepository;
    private final ConcertDeatilRepository concertDeatilRepository;

    // 콘서트 목록 조회
    public List<ConcertListResponse> getConcerts(String keyword, String sort) {
        List<Concert> concerts = concertRepository.findByKeyword(keyword);

        // 정렬
        Comparator<Concert> comparator;
        if ("latest".equals(sort)) {
            comparator = Comparator.comparing(Concert::getStartDate).reversed();
        } else {
            // 기본값: closingSoon (마감 임박순)
            comparator = Comparator.comparing(Concert::getEndDate);
        }

        return concerts.stream()
                .sorted(comparator)
                .map(concert -> {
                    String venueName = scheduleRepository
                            .findFirstByConcertConcertId(concert.getConcertId())
                            .map(schedule -> schedule.getVenue().getVenueName())
                            .orElse("");
                    return ConcertListResponse.of(concert, venueName);
                })
                .collect(Collectors.toList());
    }

    // 콘서트 상세 조회
    public ConcertDetailResponse getConcertDetail(Long concertId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 콘서트입니다."));

        Schedule schedule = scheduleRepository.findFirstByConcertConcertId(concertId)
                .orElseThrow(() -> new ServiceException("404-2", "등록된 회차가 없습니다."));

        Venue venue = schedule.getVenue();

        List<String> detailUrlList = concertDeatilRepository
                .findByConcertConcertId(concertId)
                .stream()
                .map(ConcertDetail::getUrlDetail)
                .collect(Collectors.toList());

        // 가격 정보: 첫 번째 회차의 좌석 등급별 가격
        Map<String, Integer> prices = scheduleSeatRepository
                .findByScheduleScheduleId(schedule.getScheduleId())
                .stream()
                .collect(Collectors.toMap(
                        ScheduleSeat::getGradeName,
                        ScheduleSeat::getSeatPrice,
                        (v1, v2) -> v1
                ));

        return ConcertDetailResponse.of(
                concert,
                venue.getVenueName(),
                venue.getLocation(),
                detailUrlList,
                prices
        );
    }

    // 좌석 선택 조회 (기존)
    public SeatSelectionResponse getSeatSelection(Long concertId, Long scheduleId) {
        if (!concertRepository.existsById(concertId)) {
            throw new ServiceException("404-1", "존재하지 않는 콘서트입니다.");
        }
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ServiceException("404-2", "존재하지 않는 회차입니다."));

        if (!schedule.getConcert().getConcertId().equals(concertId)) {
            throw new ServiceException("400-1", "해당 콘서트의 회차가 아닙니다.");
        }

        List<ScheduleSeat> scheduleSeats = scheduleSeatRepository.findByScheduleScheduleId(scheduleId);

        Map<String, Integer> pricesMap = scheduleSeats.stream()
                .collect(Collectors.toMap(
                        ScheduleSeat::getGradeName,
                        ScheduleSeat::getSeatPrice,
                        (v1, v2) -> v1
                ));

        List<SeatDetailResponse> seatDetailList = scheduleSeats.stream()
                .map(seat -> new SeatDetailResponse(
                        seat.getSeatNumber(),
                        seat.getSeatStatus(),
                        seat.getGradeName()
                ))
                .collect(Collectors.toList());

        return new SeatSelectionResponse(
                concertId,
                scheduleId,
                pricesMap,
                seatDetailList
        );
    }
}