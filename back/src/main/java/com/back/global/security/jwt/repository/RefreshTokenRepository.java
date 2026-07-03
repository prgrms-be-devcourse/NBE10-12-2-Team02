package com.back.global.security.jwt.repository;

import com.back.global.security.jwt.RefreshTokenLuaScripts;
import com.back.global.security.jwt.RefreshTokenRotateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {
    private final StringRedisTemplate redisTemplate;

    @Value("${custom.redis.refresh-token.prefix}")
    private String prefix;

    @Value("${custom.redis.refresh-token.index-prefix}")
    private String indexPrefix;

    public RefreshTokenRotateResult rotate(
            Long userId,
            String oldJti,
            String requestRefreshTokenHash,
            String newJti,
            String newRefreshTokenHash,
            Duration ttl
    ) {
        Long result = redisTemplate.execute(
                RefreshTokenLuaScripts.ROTATE,
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

        return switch (Math.toIntExact(result)) {
            case 1 -> RefreshTokenRotateResult.SUCCESS;
            case -1 -> RefreshTokenRotateResult.MISMATCH;
            case 0 -> RefreshTokenRotateResult.NOT_FOUND;
            default -> throw new IllegalStateException("Refresh token rotation failed");
        };
    }

    public void save(Long userId, String jti, String refreshTokenHash, Duration ttl) {
        String key = getKey(userId, jti);
        String indexKey = getIndexKey(userId);

        redisTemplate.opsForValue().set(key, refreshTokenHash, ttl);
        redisTemplate.opsForSet().add(indexKey, jti);
        redisTemplate.expire(indexKey, ttl);
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
        return indexPrefix + userId;
    }

    private String getKey(Long userId, String jti) {
        return prefix + userId + ":" + jti;
    }
}
