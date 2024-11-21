package com.palettee.global.security.jwt.services;

import com.palettee.user.domain.*;
import java.util.*;
import java.util.concurrent.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenRedisService {

    public final RedisTemplate<String, Object> redisTemplate;

    /**
     * {@code Refresh} 토큰 저장하기 위한 메서드
     * <p>
     * 값 저장할 때 {@code timeout} 넣어줘서 {@code Redis} 가 알아서 나중에 삭제해줌
     *
     * @param user      {@code Refresh} 토큰을 발급한 유저
     * @param expireMin {@code Refresh} 토큰 만료 시간 (분)
     */
    public void storeRefreshToken(final User user, final String refreshToken, long expireMin) {
        String key = getRefreshTokenKey(user);

        if (this.getRefreshToken(user).isPresent()) {
            log.debug("Prev refresh token : {}", this.getRefreshToken(user).get());
            log.debug("Current refresh token : {}", refreshToken);
            deleteRefreshToken(user);
        }

        redisTemplate.opsForValue().set(key, refreshToken, expireMin, TimeUnit.MINUTES);

        log.info("Refresh token has been stored to redis : {}", key);
    }

    /**
     * 유저가 발급했던 {@code Refresh} 토큰 가져오는 메서드
     */
    public Optional<String> getRefreshToken(final User user) {
        String key = getRefreshTokenKey(user);
        return Optional.ofNullable((String) redisTemplate.opsForValue().get(key));
    }

    public void deleteRefreshToken(final User user) {
        String key = getRefreshTokenKey(user);

        if (Boolean.TRUE.equals(redisTemplate.delete(key))) {
            log.warn("Deleted {}'s refresh token on Redis", user.getEmail());
        } else {
            log.warn("Failed to delete {}'s refresh token. : no token exists on Redis",
                    user.getEmail());
        }
    }

    private String getRefreshTokenKey(final User user) {
        return "refresh_token_" + user.getEmail();
    }
}
