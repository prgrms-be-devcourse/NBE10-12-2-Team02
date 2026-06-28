package com.back.domain.concert.service;

import com.back.domain.concert.dto.SeatSelectionDto;
import com.back.domain.concert.dto.SeatSelectionDto.SeatDetailDto;
import com.back.domain.schedule.entity.ScheduleSeat;
import com.back.domain.schedule.repository.ScheduleSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {
    private final ScheduleSeatRepository scheduleSeatRepository;

    public SeatSelectionDto getSeatSelection(Long concertId, Long scheduleId) {

        List<ScheduleSeat> scheduleSeats = scheduleSeatRepository.findByScheduleScheduleId(scheduleId);

        Map<String, Integer> pricesMap = scheduleSeats.stream()
                .collect(Collectors.toMap(
                        ScheduleSeat::getGradeName,
                        ScheduleSeat::getSeatPrice,
                        (v1, v2) -> v1
                ));

        List<SeatDetailDto> seatDetailList = scheduleSeats.stream()
                .map(seat -> new SeatDetailDto(
                        seat.getSeatNumber(),
                        seat.getSeatStatus(),
                        seat.getGradeName()
                ))
                .collect(Collectors.toList());

        return new SeatSelectionDto(
                concertId,
                scheduleId,
                pricesMap,
                seatDetailList
        );
    }
}
