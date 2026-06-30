package com.back.domain.concert.service;

import com.back.domain.concert.dto.SeatOccupyResponse;
import com.back.domain.concert.dto.SeatSelectionResponse;
import com.back.domain.concert.dto.SeatSelectionResponse.SeatDetailResponse;
import com.back.domain.concert.repository.ConcertRepository;
import com.back.domain.schedule.entity.Schedule;
import com.back.domain.schedule.entity.ScheduleSeat;
import com.back.domain.schedule.entity.SeatStatus;
import com.back.domain.schedule.repository.ScheduleRepository;
import com.back.domain.schedule.repository.ScheduleSeatRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.back.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final ScheduleRepository scheduleRepository;
    private final ConcertRepository concertRepository;
    private final StringRedisTemplate redisTemplate;

    private static final long OCCUPY_TTL_SECONDS = 600;

    private static final RedisScript<Long> OCCUPY_SCRIPT = new DefaultRedisScript<>(
            """
                    if redis.call('EXISTS', KEYS[1]) == 0 then
                      redis.call('HSET', KEYS[1], 'userId', ARGV[1], 'occupyToken', ARGV[2])
                      redis.call('EXPIRE', KEYS[1], ARGV[3])
                      return 1
                    else
                      return 0
                    end
                    """,
            Long.class
    );

    public SeatSelectionResponse getSeatSelection(Long concertId, Long scheduleId) {
        if (!concertRepository.existsById(concertId)) {
            throw new ServiceException(CONCERT_NOT_FOUND);
        }
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ServiceException(SCHEDULE_NOT_FOUND));

        if (!schedule.getConcert().getConcertId().equals(concertId)) {
            throw new ServiceException(INVALID_CONCERT_SCHEDULE);
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

    @Transactional
    public SeatOccupyResponse seatOccupy(Long concertId, Long scheduleId, String seatNumber, Long userId) {
        ScheduleSeat scheduleSeat = scheduleSeatRepository.findByScheduleScheduleIdAndSeatNumber(scheduleId, seatNumber)
                .orElseThrow(() -> new ServiceException(SEAT_NOT_FOUND));

        if (scheduleSeat.getSeatStatus() == SeatStatus.SOLD_OUT) {
            throw new ServiceException(SEAT_ALREADY_SOLD);
        }

        String redisKey = String.format("seat:occupy:%d:%d:%s", concertId, scheduleId, scheduleSeat.getSeatNumber());
        String occupyToken = UUID.randomUUID().toString();

        Long result = redisTemplate.execute(
                OCCUPY_SCRIPT,
                List.of(redisKey),
                userId.toString(),
                occupyToken,
                String.valueOf(OCCUPY_TTL_SECONDS)
        );

        if (result == null || result == 0L) {
            throw new ServiceException(SEAT_HELD_BY_OTHER_USER);
        }

        scheduleSeat.updateSeatStatus(SeatStatus.HOLD);

        return new SeatOccupyResponse(occupyToken, OCCUPY_TTL_SECONDS, SeatStatus.HOLD);
    }
}
