package com.palettee.global.security.jwt.services;

import static org.assertj.core.api.Assertions.*;

import com.palettee.global.security.dto.token.*;
import com.palettee.global.security.jwt.exceptions.*;
import com.palettee.global.security.jwt.utils.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
import java.util.function.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;

@Slf4j
@SpringBootTest
class TokenServiceTest {

    @Autowired
    TokenService tokenService;

    @Autowired
    UserRepository userRepo;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenRedisService refreshTokenRedisService;

    static User testUser;

    @BeforeEach
    void setup() {
        try {
            testUser = userRepo.save(
                    User.builder()
                            .userRole(UserRole.REAL_NEWBIE)
                            .email("test@test.com")
                            .build()
            );
        } catch (Exception ignore) {
            testUser = userRepo.findByEmail(testUser.getEmail())
                    .orElseThrow();
        }
    }

    @AfterEach
    void remove() {
        refreshTokenRedisService.deleteRefreshToken(testUser);
        userRepo.deleteAll();
    }


    @Test
    @DisplayName("임시 토큰으로 토큰 발급")
    void issueToken() {
        String temporaryToken = jwtUtils.createTemporaryToken(testUser);
        log.info("Published temporary token: {}", temporaryToken);

        TokenContainer result = tokenService.issueToken(temporaryToken);

        // 반환값 유효한지 확인
        this.checkResult(result);

        // 예외 처리 확인
        temporaryToken = jwtUtils.createTemporaryToken(testUser);   // 혹시 만료 됐을까봐 다시 생성
        this.checkExceptions(tokenService::issueToken, temporaryToken);
    }

    @Test
    @DisplayName("Refresh 토큰으로 토큰 발급")
    void reissueToken() {
        String refreshToken = jwtUtils.createRefreshToken(testUser);
        log.info("Created refresh token: {}", refreshToken);

        refreshTokenRedisService.storeRefreshToken(testUser, refreshToken, 10L);
        log.info("Saved token to redis: {}", refreshToken);

        // jwt 발급 시간 정밀도는 초단위라 1 초 기다림
        try {
            Thread.sleep(1000);
        } catch (Exception ignore) {
        }

        TokenContainer result = tokenService.reissueToken(refreshToken);

        // 반환값 유효한지 확인
        this.checkResult(result);

        // 새로운 refresh 토큰이 redis 에 저장됐는지 확인
        assertThat(refreshTokenRedisService.getRefreshToken(testUser).orElseThrow())
                .isNotEqualTo(refreshToken);

        // 예외 확인
        this.checkExceptions(tokenService::reissueToken, refreshToken);
    }


    private void checkResult(TokenContainer result) {
        // not null
        assertThat(result).isNotNull();

        String access = result.accessToken();
        String refresh = result.refreshToken();
        long expireSec = result.expiresInSeconds();

        // not null & 만료시간 제대로 됬는지 확인
        assertThat(access).isNotNull();
        assertThat(refresh).isNotNull();
        assertThat(expireSec).isGreaterThan(0)
                .isEqualTo(60 * jwtUtils.getAccessExpireMin());

        // refresh redis 에 저장된 값이랑 동일한지 확인
        String redisToken = refreshTokenRedisService.getRefreshToken(testUser).orElse(null);
        assertThat(redisToken).isNotNull().isEqualTo(refresh);

        log.info("Dto result is valid.");
    }

    private void checkExceptions(Function<String, ?> serviceMethod, String validToken) {

        // jwt 가 존재하지 않음
        assertThatThrownBy(() -> serviceMethod.apply(null))
                .isInstanceOf(NoTokenExistsException.class);
        assertThatThrownBy(() -> serviceMethod.apply(""))
                .isInstanceOf(NoTokenExistsException.class);

        // jwt 가 유효하지 않음
        assertThatThrownBy(() -> serviceMethod.apply("RandomInvalidToken"))
                .isInstanceOf(InvalidTokenException.class);

        // jwt 는 유효하나 유저를 찾을 수 없음
        userRepo.delete(testUser);
        assertThatThrownBy(() -> serviceMethod.apply(validToken))
                .isInstanceOf(NoUserFoundViaTokenException.class);

        testUser = userRepo.save(testUser);

        log.info("All exceptions are covered.");
    }
}