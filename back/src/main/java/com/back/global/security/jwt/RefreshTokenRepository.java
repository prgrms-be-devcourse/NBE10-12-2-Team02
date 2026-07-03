package com.back.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {
    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "auth:refresh:";
    private static final String INDEX_PREFIX = "auth:refresh-index:";

    private static final DefaultRedisScript<Long> ROTATE_SCRIPT = new DefaultRedisScript<>(
            """
            local oldValue = redis.call('GET', KEYS[1])
    
            if not oldValue then
                return 0
            end
    
            if oldValue ~= ARGV[1] then
                return -1
            end
    
            redis.call('DEL', KEYS[1])
            redis.call('SREM', KEYS[3], ARGV[4])
    
            redis.call('SET', KEYS[2], ARGV[2], 'EX', ARGV[3])
            redis.call('SADD', KEYS[3], ARGV[5])
            redis.call('EXPIRE', KEYS[3], ARGV[3])
    
            return 1
            """,
            Long.class
    );

    public enum RotateResult {
        SUCCESS,
        NOT_FOUND,
        MISMATCH
    }

    public RotateResult rotate(
            Long userId,
            String oldJti,
            String requestRefreshTokenHash,
            String newJti,
            String newRefreshTokenHash,
            Duration ttl
    ) {
        Long result = redisTemplate.execute(
                ROTATE_SCRIPT,
                List.of(
                        getKey(userId, oldJti),
                        getKey(userId, newJti),
                        getIndexKey(userId)
                ),
                requestRefreshTokenHash,
                newRefreshTokenHash,
                String.valueOf(ttl.toSeconds()),
                oldJti,
                newJti
        );
        if (result == null) {
            throw new IllegalStateException("Refresh token rotation failed");
        }

        if (result == 1L) {
            return RotateResult.SUCCESS;
        }

        if (result == -1L) {
            return RotateResult.MISMATCH;
        }

        return RotateResult.NOT_FOUND;
    }

        public void save(Long userId, String jti, String refreshTokenHash, Duration ttl) {
        String key = getKey(userId, jti);
        String indexKey = getIndexKey(userId);

        redisTemplate.opsForValue().set(key, refreshTokenHash, ttl);
        redisTemplate.opsForSet().add(indexKey, jti);
        redisTemplate.expire(indexKey, ttl);
    }

    public String find(Long userId, String jti) {
        String key = getKey(userId, jti);
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            redisTemplate.opsForSet().remove(getIndexKey(userId), jti);
        }

        return value;
    }

    public void delete(Long userId, String jti) {
        redisTemplate.delete(getKey(userId, jti));
        redisTemplate.opsForSet().remove(getIndexKey(userId), jti);
    }

    public void deleteAllByUserId(Long userId) {
        String indexKey = getIndexKey(userId);
        Set<String> jtis = redisTemplate.opsForSet().members(indexKey);

        if (jtis == null || jtis.isEmpty()) {
            return;
        }

        List<String> keys = jtis.stream()
                .map(jti -> getKey(userId, jti))
                .toList();

        redisTemplate.delete(keys);
        redisTemplate.delete(indexKey);
    }

    private String getIndexKey(Long userId) {
        return INDEX_PREFIX + userId;
    }

    private String getKey(Long userId, String jti) {
        return PREFIX + userId + ":" + jti;
    }
}