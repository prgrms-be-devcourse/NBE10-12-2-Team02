package com.back.domain.waiting;

import com.back.domain.waiting.dto.WaitingQueueRegisterResponse;
import com.back.global.exception.ErrorCode;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class WaitingQueueManager {
    private final StringRedisTemplate redisTemplate;

    //TODO 현재는 이미 등록된 사용자가 재요청 해도 Sequence 증가함 -> Lua script로 사용자 없을때만 INCR + ZADD를 묶자
    public WaitingQueueRegisterResponse registerWaiting(Long concertId,Long scheduleId,Long userId) {
        String waitKey = generateWaitKey(scheduleId);
        String seqKey = generateSequenceKey(scheduleId);
        String user = userId.toString();

        Long sequence = redisTemplate.opsForValue().increment(seqKey);
        if (sequence == null) {
           throw new ServiceException(ErrorCode.WAITING_QUEUE_REGISTER_FAILED);
        }
        Boolean registered = redisTemplate.opsForZSet()
                .addIfAbsent(waitKey, user, sequence.doubleValue());

        Long rank = redisTemplate.opsForZSet().rank(waitKey, user);
        if (rank == null) {
            throw new ServiceException(ErrorCode.WAITING_QUEUE_REGISTER_FAILED);
        }
        return new WaitingQueueRegisterResponse(concertId, scheduleId, userId, rank + 1,
                Boolean.TRUE.equals(registered));
    }

    private String generateWaitKey(Long scheduleId) {
        return "queue:wait:schedule:" + scheduleId;
    }
    private String generateSequenceKey(Long scheduleId) {
        return "queue:wait:seq:schedule:" + scheduleId;
    }
}
