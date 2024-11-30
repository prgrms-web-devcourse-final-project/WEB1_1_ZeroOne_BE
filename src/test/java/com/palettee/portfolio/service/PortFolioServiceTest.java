package com.palettee.portfolio.service;

import com.palettee.global.cache.MemoryCache;
import com.palettee.global.redis.service.RedisService;
import com.palettee.likes.domain.LikeType;
import com.palettee.likes.domain.Likes;
import com.palettee.likes.repository.LikeRepository;
import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioLikeResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioResponse;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.portfolio.repository.PortFolioRepository;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import com.palettee.user.domain.User;
import com.palettee.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.palettee.global.Const.LIKE_PREFIX;
import static com.palettee.global.Const.VIEW_PREFIX;

@SpringBootTest
class PortFolioServiceTest {

    @Autowired
    private PortFolioService portFolioService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PortFolioRepository portFolioRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MemoryCache memoryCache;


    private User user;
    private PortFolio portFolio;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .imageUrl("image")
                .email("hello")
                .name("테스트")
                .briefIntro("안녕하세요")
                .majorJobGroup(MajorJobGroup.DEVELOPER)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();
        userRepository.save(user);

        portFolio = PortFolio.builder()
                .user(user)
                .url("테스트테스트")
                .build();
        portFolioRepository.save(portFolio);
    }

    @AfterEach
    void tearDown() {
        portFolioRepository.deleteAll();
        userRepository.deleteAll();
        likeRepository.deleteAll();
        RedisTemplate<String, Long> redisTemplate = redisService.getRedisTemplate();
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("포트폴리오 전체조회 무한스크롤 처리")
    void portfolio_pageNation() {
        // given
        for (int i = 0; i < 20; i++) {
            PortFolio portFolio = PortFolio.builder()
                    .user(user)
                    .url("테스트테스트1")
                    .build();
            portFolioRepository.save(portFolio);
        }

        // when
        List<PortFolio> all = portFolioRepository.findAll();
        System.out.println(all.size());

        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<PortFolioResponse> results = portFolioService.findAllPortFolio(
                pageRequest,
                MajorJobGroup.DEVELOPER.getMajorGroup(),
                MinorJobGroup.BACKEND.getMinorJobGroup(),
                "popularlity"
        );

        // then
        Assertions.assertThat(results.getSize()).isEqualTo(10);
        Assertions.assertThat(results.hasNext()).isEqualTo(true);
    }

    @Test
    @DisplayName("좋아요한 포트폴리오 목록 조회 NoOffset")
    void userLike_portFolio() {
        // given
        for (int i = 0; i < 20; i++) {
            PortFolio portFolio = PortFolio.builder()
                    .user(user)
                    .url("테스트테스트1")
                    .build();
            portFolioRepository.save(portFolio);

            Likes likes = Likes.builder()
                    .targetId(portFolio.getPortfolioId())
                    .user(user)
                    .likeType(LikeType.PORTFOLIO)
                    .build();
            likeRepository.save(likes);
        }

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        CustomSliceResponse customSliceResponse = portFolioService.findListPortFolio(pageRequest, user.getId(), null);

        // then
        Assertions.assertThat(customSliceResponse.content().size()).isEqualTo(10);
        Assertions.assertThat(customSliceResponse.hasNext()).isEqualTo(true);
    }

    @Test
    @DisplayName("포트폴리오 좋아요 생성")
    void portFolio_Like_Create() {
        // given
        PortFolioLikeResponse portFolioLike = portFolioService.createPortFolioLike(portFolio.getPortfolioId(), user);

        // when
        Likes likes = likeRepository.findById(portFolioLike.portFolioId()).orElseThrow();

        // then
        Assertions.assertThat(likes.getLikeType()).isEqualTo(LikeType.PORTFOLIO);
    }

    @Test
    @DisplayName("포트 폴리오 좋아요 취소")
    public void portFolio_Like_Cancel() throws Exception {
       //given
        PortFolioLikeResponse portFolioLike = portFolioService.createPortFolioLike(portFolio.getPortfolioId(), user);

       //when
        PortFolioLikeResponse portFolioLik1 = portFolioService.createPortFolioLike(portFolio.getPortfolioId(), user);

        Optional<Likes> findByLikes = likeRepository.findById(portFolioLike.portFolioId());

        //then
        Assertions.assertThat(findByLikes.isPresent()).isEqualTo(false);
    }

    @Test
    @DisplayName("Redis를 사용한 포트폴리오 조회수 증가 및 DB 반영 테스트")
    public void portFolio_hits_redis() throws Exception {
       //given

        RedisTemplate<String, Long> redisTemplate = redisService.getRedisTemplate();

        redisTemplate.delete(VIEW_PREFIX + "portFolio" + ": " + portFolio.getPortfolioId());

        for(int i =0 ; i < 5; i++){
            redisService.viewCount(portFolio.getPortfolioId(), "portFolio");
        }

       //when

        redisService.viewRedisToDB(VIEW_PREFIX + "portFolio" + ": ");

        Map<String, Long> localCache = memoryCache.getLocalCache();

        PortFolio portFolio1 = portFolioRepository.findById(portFolio.getPortfolioId()).get();



        Long remainCount = redisTemplate.opsForValue().get(VIEW_PREFIX + "portFolio" + ": " + portFolio.getPortfolioId());

        Long cacheCount = localCache.get(VIEW_PREFIX + "portFolio" + ": " + portFolio.getPortfolioId());

        //then
        Assertions.assertThat(portFolio1.getHits()).isEqualTo(5);
        Assertions.assertThat(remainCount).isEqualTo(0);
        Assertions.assertThat(cacheCount).isEqualTo(5);

    }

    @Test
    @DisplayName("redis를 활용한 포트 폴리오 좋아요")
    public void portFolio_like() throws Exception {
       //given
        RedisTemplate<String, Long> redisTemplate = redisService.getRedisTemplate();

        redisTemplate.delete( LIKE_PREFIX + "portFolio: " + portFolio.getPortfolioId() + "_user");

        redisTemplate.delete(LIKE_PREFIX + "portFolio: " + portFolio.getPortfolioId());



        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요

       //when


        Long count = redisTemplate.opsForValue().get(LIKE_PREFIX + "portFolio: " + portFolio.getPortfolioId());

        Set<Long> members = redisTemplate.opsForSet().members(LIKE_PREFIX + "portFolio: " + portFolio.getPortfolioId() + "_user");

        //then
        Assertions.assertThat(count).isEqualTo(1);
        Assertions.assertThat(members).hasSize(1);

    }


    @Test
    @DisplayName("redis를 활용한 포트 폴리오 좋아요와 중복 좋아요 체크")
    public void Duration_portFolio_like() throws Exception {
        //given
        RedisTemplate<String, Long> redisTemplate = redisService.getRedisTemplate();

       redisTemplate.getConnectionFactory().getConnection().flushAll();


        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요

        //when

        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요

        Long count = redisTemplate.opsForValue().get(LIKE_PREFIX + "portFolio: " + portFolio.getPortfolioId());

        Set<Long> members = redisTemplate.opsForSet().members(LIKE_PREFIX + "portFolio: " + portFolio.getPortfolioId() + "_user");

        //then
        Assertions.assertThat(count).isEqualTo(0);
        Assertions.assertThat(members).hasSize(0);

    }

    @Test
    @DisplayName("redis 를 사용하여 유저가 좋아요 db batch insert")
    public void redis_like_db() throws Exception {
       //given
        RedisTemplate<String, Long> redisTemplate = redisService.getRedisTemplate();

        redisTemplate.getConnectionFactory().getConnection().flushAll();

        User user1 = User.builder()
                .imageUrl("image")
                .email("hellod")
                .name("테스트")
                .briefIntro("안녕하세요")
                .majorJobGroup(MajorJobGroup.DEVELOPER)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();

        User user2 = User.builder()
                .imageUrl("image")
                .email("hellos")
                .name("테스트")
                .briefIntro("안녕하세요")
                .majorJobGroup(MajorJobGroup.DEVELOPER)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();

        userRepository.save(user1);

        userRepository.save(user2);

        //when

        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요
        redisService.likeCount(portFolio.getPortfolioId(), user1.getId(), "portFolio");  // 포트폴리오 유저 좋아요
        redisService.likeCount(portFolio.getPortfolioId(), user2.getId(), "portFolio");  // 포트폴리오 유저 좋아요


        redisService.likeRedisToDB( LIKE_PREFIX + "portFolio:", "portFolio" );

        Map<String, Long> localCache = memoryCache.getLocalCache();

        Long aw = localCache.get(LIKE_PREFIX + "portFolio:" + portFolio.getPortfolioId());


        List<Likes> byTargetId = likeRepository.findByTargetId(portFolio.getPortfolioId());

        //then
        Assertions.assertThat(byTargetId).hasSize(3);
        Assertions.assertThat(aw).isEqualTo(15);


    }
}
