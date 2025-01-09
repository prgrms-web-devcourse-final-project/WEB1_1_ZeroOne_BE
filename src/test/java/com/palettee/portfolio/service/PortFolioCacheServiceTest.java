package com.palettee.portfolio.service;

import com.palettee.portfolio.controller.dto.response.CustomOffSetResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioResponse;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.portfolio.repository.PortFolioRedisRepository;
import com.palettee.portfolio.repository.PortFolioRepository;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import com.palettee.user.domain.User;
import com.palettee.user.domain.UserRole;
import com.palettee.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;
import java.util.Set;

import static com.palettee.portfolio.repository.PortFolioRedisRepository.RedisConstKey_PortFolio;

@SpringBootTest
public class PortFolioCacheServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PortFolioCacheServiceTest.class);
    @Autowired
    private PortFolioService portFolioService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PortFolioRepository portFolioRepository;

    @Autowired
    private RedisTemplate<String, PortFolioResponse> redisTemplate;

    @Autowired
    private PortFolioRedisRepository redisRepository;

    private User user;

    private PortFolio portFolio;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .imageUrl("image")
                .email("hello")
                .name("테스트")
                .briefIntro("안녕하세요")
                .userRole(UserRole.USER)
                .majorJobGroup(MajorJobGroup.DEVELOPER)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();
        userRepository.save(user);

        portFolio = PortFolio.builder()
                .user(user)
                .url("테스트테스트")
                .majorJobGroup(MajorJobGroup.DEVELOPER)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();
        portFolioRepository.save(portFolio);
    }

    @AfterEach
    void tearDown() {
        portFolioRepository.deleteAll();
        userRepository.deleteAll();

        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("첫 번쨰 페이지 zset 캐싱")
    public void 포트폴리오_캐싱() throws Exception {
        // given
        for (int i = 0; i < 20; i++) {
            PortFolio portFolio = PortFolio.builder()
                    .user(user)
                    .url("테스트테스트1")
                    .majorJobGroup(MajorJobGroup.DEVELOPER)
                    .minorJobGroup(MinorJobGroup.BACKEND)
                    .build();
            portFolioRepository.save(portFolio);
        }

        //when
        PageRequest pageRequest = PageRequest.of(0, 10);
        CustomOffSetResponse results = portFolioService.findAllPortFolio(
                pageRequest,
                null,
                null,
                "latest"
                ,true
        );

        Set<PortFolioResponse> range = redisTemplate.opsForZSet().range(RedisConstKey_PortFolio, 0, -1);

        //then
        Assertions.assertThat(range.size()).isEqualTo(10);
    }


    @Test
    @DisplayName("포트폴리오 등록 캐시 정합성")
    public void 포트폴리오_등록_캐시정합성() throws Exception {
       //given
        for(int i =0; i < 5; i ++){
            User testUser = User.builder()
                    .imageUrl("image")
                    .name("테스트")
                    .email("hello" + i)
                    .briefIntro("안녕하세요")
                    .userRole(UserRole.USER)
                    .majorJobGroup(MajorJobGroup.MARKETING)
                    .minorJobGroup(MinorJobGroup.BACKEND)
                    .build();

            PortFolio testPortFolio = PortFolio.builder()
                    .user(testUser)
                    .url("테스트테스트1")
                    .majorJobGroup(testUser.getMajorJobGroup())
                    .minorJobGroup(MinorJobGroup.BACKEND)
                    .build();

            userRepository.save(testUser);
            portFolioRepository.save(testPortFolio);
        }
       //when
        PageRequest pageRequest = PageRequest.of(0, 6);
        portFolioService.findAllPortFolio(
                pageRequest,
                null,
                null,
                "latest"
                ,true  //캐싱 여부
        );

       portFolio = PortFolio.builder()
                .user(user)
                .url("새로운거지롱")
                .majorJobGroup(MajorJobGroup.DESIGN)
                .minorJobGroup(MinorJobGroup.SERVICE)
                .build();
        PortFolio save = portFolioRepository.save(portFolio);

        redisRepository.addPortFolioInRedis(save.getPortfolioId());


        Set<PortFolioResponse> range = redisTemplate.opsForZSet().range(RedisConstKey_PortFolio, 0, -1);

        Optional<PortFolioResponse> samplePortFolio = range.stream().filter(portFolioResponse -> portFolioResponse.getUserId().equals(save.getUser().getId())).findFirst();


        System.out.println("userId = "+ samplePortFolio.get().getUserId());


        //then
        Assertions.assertThat(range.size()).isEqualTo(6);
        Assertions.assertThat(samplePortFolio.isPresent()).isTrue();
        Assertions.assertThat(samplePortFolio.get().getMajorJobGroup()).isEqualTo(MajorJobGroup.DESIGN.name());
    }

    @Test
    @DisplayName("포트폴리오_수정_캐시 정합성")
    public void 퐅트폴리오_수정_캐시정합성() throws Exception {
        //given
        for(int i =0; i < 5; i ++){
            User testUser = User.builder()
                    .imageUrl("image")
                    .name("테스트")
                    .email("hello" + i)
                    .briefIntro("안녕하세요")
                    .userRole(UserRole.USER)
                    .majorJobGroup(MajorJobGroup.DEVELOPER)
                    .minorJobGroup(MinorJobGroup.BACKEND)
                    .build();

            PortFolio testPortFolio = PortFolio.builder()
                    .user(testUser)
                    .url("테스트테스트1")
                    .majorJobGroup(MajorJobGroup.DEVELOPER)
                    .minorJobGroup(MinorJobGroup.BACKEND)
                    .build();

            userRepository.save(testUser);
            portFolioRepository.save(testPortFolio);
        }
        //when
        PageRequest pageRequest = PageRequest.of(0, 6);
        portFolioService.findAllPortFolio(
                pageRequest,
                null,
                null,
                "latest"
                ,true  //캐싱 여부
        );

        // 새로 만들어진 포트폴리오
        PortFolio updateNewPortFolio= PortFolio.builder()
                .user(user)
                .url("수정된 유저 포트폴리오")
                .majorJobGroup(MajorJobGroup.ETC)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();

        portFolioRepository.save(updateNewPortFolio);

        redisRepository.updatePortFolio(updateNewPortFolio.getPortfolioId());

        Set<PortFolioResponse> range = redisTemplate.opsForZSet().range(RedisConstKey_PortFolio, 0, -1);

        Optional<PortFolioResponse> samplePortFolio = range.stream().filter(portFolioResponse -> portFolioResponse.getUserId().equals(updateNewPortFolio.getUser().getId())).findFirst();

        //then
        Assertions.assertThat(range.size()).isEqualTo(6);
        Assertions.assertThat(samplePortFolio.isPresent()).isEqualTo(true);
        Assertions.assertThat(samplePortFolio.get().getMajorJobGroup()).isEqualTo(MajorJobGroup.ETC.name());
    }

    @Test
    @DisplayName("포트폴리오 추가 시 사이즈 레디스 내부 size 만큼 없으면 삭제 작업 없음")
    public void 포트폴리오_캐시확인후_삭제작업없음() throws Exception {
        //given
        for(int i =0; i < 5; i ++){
            User testUser = User.builder()
                    .imageUrl("image")
                    .name("테스트")
                    .email("hello" + i)
                    .briefIntro("안녕하세요")
                    .userRole(UserRole.USER)
                    .majorJobGroup(MajorJobGroup.DEVELOPER)
                    .minorJobGroup(MinorJobGroup.BACKEND)
                    .build();

            PortFolio testPortFolio = PortFolio.builder()
                    .user(testUser)
                    .url("테스트테스트1")
                    .majorJobGroup(MajorJobGroup.DEVELOPER)
                    .minorJobGroup(MinorJobGroup.BACKEND)
                    .build();

            userRepository.save(testUser);
            portFolioRepository.save(testPortFolio);
        }
        //when
        PageRequest pageRequest = PageRequest.of(0, 8);
        portFolioService.findAllPortFolio(
                pageRequest,
                null,
                null,
                "latest"
                ,true  //캐싱 여부
        );

        User newUser = User.builder()
                .imageUrl("image")
                .name("테스트")
                .email("ㄹㅇㄹㅇ")
                .briefIntro("안녕하세요")
                .userRole(UserRole.USER)
                .majorJobGroup(MajorJobGroup.DEVELOPER)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();

        userRepository.save(newUser);

        PortFolio updateNewPortFolio= PortFolio.builder()
                .user(newUser)
                .url("수정된 유저 포트폴리오")
                .majorJobGroup(MajorJobGroup.ETC)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();

        PortFolio save = portFolioRepository.save(updateNewPortFolio);

        redisRepository.addPortFolioInRedis(save.getPortfolioId());

        Set<PortFolioResponse> range = redisTemplate.opsForZSet().range(RedisConstKey_PortFolio, 0, -1);

        //then
        Assertions.assertThat(range.size()).isEqualTo(7);
    }

    @Test
    @DisplayName("수정시 redis내에 유저의 값이 있으면 삭제")
    public void 수정시_유저여부() throws Exception {
        //given
        for(int i =0; i < 5; i ++){
            User testUser = User.builder()
                    .imageUrl("image")
                    .name("테스트")
                    .email("hello" + i)
                    .briefIntro("안녕하세요")
                    .userRole(UserRole.USER)
                    .majorJobGroup(MajorJobGroup.DEVELOPER)
                    .minorJobGroup(MinorJobGroup.BACKEND)
                    .build();

            PortFolio testPortFolio = PortFolio.builder()
                    .user(testUser)
                    .url("테스트테스트1")
                    .majorJobGroup(MajorJobGroup.DEVELOPER)
                    .minorJobGroup(MinorJobGroup.BACKEND)
                    .build();

            userRepository.save(testUser);
            portFolioRepository.save(testPortFolio);
        }
        //when
        PageRequest pageRequest = PageRequest.of(0, 6);
        portFolioService.findAllPortFolio(
                pageRequest,
                null,
                null,
                "latest"
                ,true  //캐싱 여부
        );

        User newUser = User.builder()
                .imageUrl("image")
                .name("테스트")
                .email("ㄹㅇㄹㅇ")
                .briefIntro("안녕하세요")
                .userRole(UserRole.USER)
                .majorJobGroup(MajorJobGroup.DEVELOPER)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();

        userRepository.save(newUser);

        PortFolio updateNewPortFolio= PortFolio.builder()
                .user(newUser)
                .url("수정된 유저 포트폴리오")
                .majorJobGroup(MajorJobGroup.ETC)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();

        PortFolio save = portFolioRepository.save(updateNewPortFolio);

        redisRepository.updatePortFolio(save.getPortfolioId());

        Set<PortFolioResponse> range = redisTemplate.opsForZSet().range(RedisConstKey_PortFolio, 0, -1);

        Optional<PortFolioResponse> samplePortFolio = range.stream().filter(portFolioResponse -> portFolioResponse.getUserId().equals(save.getUser().getId())).findFirst();


        //then
        Assertions.assertThat(range.size()).isEqualTo(6);
        Assertions.assertThat(samplePortFolio.isPresent()).isEqualTo(false);
    }
}
