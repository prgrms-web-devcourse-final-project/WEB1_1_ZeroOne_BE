package com.palettee.portfolio.service;

import com.palettee.gathering.controller.dto.Request.GatheringCommonRequest;
import com.palettee.gathering.controller.dto.Response.GatheringCommonResponse;
import com.palettee.gathering.controller.dto.Response.GatheringPopularResponse;
import com.palettee.gathering.service.GatheringService;
import com.palettee.global.cache.RedisWeightCache;
import com.palettee.global.redis.service.RedisService;
import com.palettee.likes.domain.LikeType;
import com.palettee.likes.domain.Likes;
import com.palettee.likes.repository.LikeRepository;
import com.palettee.portfolio.controller.dto.response.CustomOffSetResponse;
import com.palettee.portfolio.controller.dto.response.CustomPortFolioResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioPopularResponse;
import com.palettee.portfolio.domain.PortFolio;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private RedisWeightCache redisCache;

    @Autowired
    private GatheringService gatheringService;

    @Autowired
    private  RedisTemplate<String, Long> redisTemplate;

    private final String constView= VIEW_PREFIX + "portFolio:";

    private final String constLike = LIKE_PREFIX+ "portFolio:";


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
                    .majorJobGroup(MajorJobGroup.DEVELOPER)
                    .minorJobGroup(MinorJobGroup.BACKEND)
                    .build();
            portFolioRepository.save(portFolio);
        }

        // when
        List<PortFolio> all = portFolioRepository.findAll();
        System.out.println(all.size());

        PageRequest pageRequest = PageRequest.of(0, 10);
        CustomOffSetResponse results = portFolioService.findAllPortFolio(
                pageRequest,
                MajorJobGroup.DEVELOPER.getMajorGroup(),
                MinorJobGroup.BACKEND.getMinorJobGroup(),
                "popularlity"
                ,true
        );
        System.out.println(results.hasNext());

        // then
        Assertions.assertThat(results.pageSize()).isEqualTo(10);
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
                    .majorJobGroup(MajorJobGroup.DEVELOPER)
                    .minorJobGroup(MinorJobGroup.BACKEND)
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
        CustomPortFolioResponse customSliceResponse = portFolioService.findListPortFolio(pageRequest, user.getId(), null);

        // then
        Assertions.assertThat(customSliceResponse.content().size()).isEqualTo(10);
        Assertions.assertThat(customSliceResponse.hasNext()).isEqualTo(true);
    }


    @Test
    @DisplayName("포트 폴리오 좋아요 취소")
    public void portFolio_Like_Cancel() throws Exception {
       //given
        String setKeys = constLike + portFolio.getPortfolioId() + "_user";

        String key = constLike + portFolio.getPortfolioId();

       //when
        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요

        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요

        redisService.likeRedisToDB( constLike, "portFolio" );

        Long count = redisTemplate.opsForValue().get(key);

        List<Likes> likes = likeRepository.findByTargetId(portFolio.getPortfolioId());

        //then
        Assertions.assertThat(count).isEqualTo(0);
        Assertions.assertThat(likes.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Redis를 사용한 포트폴리오 중복 조회수 증가 및 DB 반영 테스트")
    public void portFolio_hits_redis() throws Exception {
       //given

        RedisTemplate<String, Long> redisTemplate = redisService.getRedisTemplate();


        for(int i =0 ; i < 5; i++){
            redisService.viewCount(portFolio.getPortfolioId(), user.getId(),"portFolio");
        }

       //when

        // DB 반영
        redisService.viewRedisToDB(constView, "portFolio");


        Map<Long, Long> cache = redisCache.getCache(constView);

        PortFolio portFolio1 = portFolioRepository.findById(portFolio.getPortfolioId()).get();


        Long remainCount = redisTemplate.opsForValue().get(constView + portFolio1.getPortfolioId());

        Long cacheCount = cache.get(portFolio1.getPortfolioId());

        //then
        // 조회수는 1이여야 함
        Assertions.assertThat(portFolio1.getHits()).isEqualTo(1);
        Assertions.assertThat(remainCount).isEqualTo(0);
        Assertions.assertThat(cacheCount).isEqualTo(1);

    }

    @Test
    @DisplayName("redis를 활용한 포트 폴리오 좋아요")
    public void portFolio_like() throws Exception {
       //given
        RedisTemplate<String, Long> redisTemplate = redisService.getRedisTemplate();

        String setKeys = constLike + portFolio.getPortfolioId() + "_user";

        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요

       //when
        Long count = redisTemplate.opsForValue().get(constLike + portFolio.getPortfolioId());

        Set<Long> members = redisTemplate.opsForSet().members(setKeys);

        //then
        Assertions.assertThat(count).isEqualTo(1);
        Assertions.assertThat(members).hasSize(1);

    }


    @Test
    @DisplayName("redis를 활용한 포트 폴리오 좋아요와 중복 좋아요 체크")
    public void Duration_portFolio_like() throws Exception {
        //given
        RedisTemplate<String, Long> redisTemplate = redisService.getRedisTemplate();


        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요

        //when

        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요

        Long count = redisTemplate.opsForValue().get(constLike + portFolio.getPortfolioId());

        Set<Long> members = redisTemplate.opsForSet().members(constLike + "_user");

        //then
        Assertions.assertThat(count).isEqualTo(0);
        Assertions.assertThat(members).hasSize(0);

    }

    @Test
    @DisplayName("redis 를 사용하여 유저가 좋아요 db batch insert")
    public void redis_like_db() throws Exception {
       //given
        User user1 = User.builder()
                .imageUrl("image")
                .email("hellod")
                .name("테스트")
                .briefIntro("안녕하세요")
                .userRole(UserRole.USER)
                .majorJobGroup(MajorJobGroup.DEVELOPER)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();

        User user2 = User.builder()
                .imageUrl("image")
                .email("hellos")
                .name("테스트")
                .briefIntro("안녕하세요")
                .userRole(UserRole.USER)
                .majorJobGroup(MajorJobGroup.DEVELOPER)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();

        userRepository.save(user1);

        userRepository.save(user2);


        String keys = constLike + portFolio.getPortfolioId();


        //when

        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요
        redisService.likeCount(portFolio.getPortfolioId(), user1.getId(), "portFolio");  // 포트폴리오 유저 좋아요
        redisService.likeCount(portFolio.getPortfolioId(), user2.getId(), "portFolio");  // 포트폴리오 유저 좋아요


        redisService.likeRedisToDB( constLike, "portFolio" );

        Map<Long, Long> cache = redisCache.getCache(keys);

        Long aw = cache.get(portFolio.getPortfolioId());


        List<Likes> byTargetId = likeRepository.findByTargetId(portFolio.getPortfolioId());

        //then
        Assertions.assertThat(byTargetId).hasSize(3);
        Assertions.assertThat(aw).isEqualTo(15);


    }


    @Test
    @DisplayName("인기 포폴 RedisZset을 활용한 누적 점수 합산 후 순위 메기기")
    public void reids_score() throws Exception {
        //given

        User user1 = User.builder()
                .imageUrl("image")
                .email("hellod")
                .name("테스트")
                .briefIntro("안녕하세요")
                .userRole(UserRole.USER)
                .majorJobGroup(MajorJobGroup.DEVELOPER)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();

        User user2 = User.builder()
                .imageUrl("image")
                .email("hellos")
                .name("테스트")
                .briefIntro("안녕하세요")
                .userRole(UserRole.USER)
                .majorJobGroup(MajorJobGroup.DEVELOPER)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();

        PortFolio portFolio1 = PortFolio.builder()
                .user(user)
                .url("테스트테스트")
                .build();

        PortFolio portFolio2 = PortFolio.builder()
                .user(user)
                .url("테스트테스트")
                .build();


        portFolioRepository.save(portFolio1);

        portFolioRepository.save(portFolio2);

        userRepository.save(user1);

        userRepository.save(user2);

        //when

        // 포트폴리오1좋아요 15점
        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요
        redisService.likeCount(portFolio.getPortfolioId(), user1.getId(), "portFolio");  // 포트폴리오 유저 좋아요
        redisService.likeCount(portFolio.getPortfolioId(), user2.getId(), "portFolio");  // 포트폴리오 유저 좋아요

        //프토플리오2 좋아요 10점
        redisService.likeCount(portFolio1.getPortfolioId(), user1.getId(), "portFolio");  // 포트폴리오 유저 좋아요
        redisService.likeCount(portFolio1.getPortfolioId(), user2.getId(), "portFolio");  // 포트폴리오 유저 좋아요

        //포트폴리오3 조회수 1점
        redisService.viewCount(portFolio2.getPortfolioId(), user.getId(),"portFolio");


        redisService.likeRedisToDB(constLike, "portFolio" );
        redisService.viewRedisToDB(constView, "portFolio");

        redisService.rankingCategory("portFolio");

        List<PortFolioPopularResponse> portFolioPopularResponses = portFolioService.popularPortFolio(Optional.empty()).portfolioResponses();


        //then
        Assertions.assertThat(portFolioPopularResponses.size()).isEqualTo(3);
        Assertions.assertThat(portFolioPopularResponses.get(0).getScore()).isEqualTo(15);
        Assertions.assertThat(portFolioPopularResponses.get(1).getScore()).isEqualTo(10);
        Assertions.assertThat(portFolioPopularResponses.get(2).getScore()).isEqualTo(1);
    }

    @Test
    @DisplayName("인기 포폴과 게더링 실시간 반영")
    public void getPopularService() throws Exception {
        //given

        User user1 = User.builder()
                .imageUrl("image")
                .email("hellod")
                .name("테스트")
                .briefIntro("안녕하세요")
                .userRole(UserRole.USER)
                .majorJobGroup(MajorJobGroup.DEVELOPER)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();

        User user2 = User.builder()
                .imageUrl("image")
                .email("hellos")
                .name("테스트")
                .briefIntro("안녕하세요")
                .userRole(UserRole.USER)
                .majorJobGroup(MajorJobGroup.DEVELOPER)
                .minorJobGroup(MinorJobGroup.BACKEND)
                .build();

        PortFolio portFolio1 = PortFolio.builder()
                .user(user)
                .url("테스트테스트")
                .build();

        PortFolio portFolio2 = PortFolio.builder()
                .user(user)
                .url("테스트테스트")
                .build();

        List<String> tagList = new ArrayList<>();

        tagList.add("tag1");
        tagList.add("tag2");


        List<String> imageList = new ArrayList<>();
        imageList.add("URL1");
        imageList.add("URL2");


        List<String> positions = new ArrayList<>();
        positions.add("개발자");
        positions.add("기획자");

        GatheringCommonRequest gatheringCreateRequest1 = new GatheringCommonRequest("프로젝트", "개발", "온라인", 3, "3개월", "2024-11-24", positions, tagList, "testUrl", "제목", "content",null);

        GatheringCommonRequest gatheringCreateRequest2 = new GatheringCommonRequest("프로젝트", "개발", "온라인", 3, "3개월", "2024-11-24", positions, tagList, "testUrl", "제목", "content",null);
        GatheringCommonResponse gathering1 = gatheringService.createGathering(gatheringCreateRequest1, user);
        GatheringCommonResponse gathering2 = gatheringService.createGathering(gatheringCreateRequest2, user);


        portFolioRepository.save(portFolio1);

        portFolioRepository.save(portFolio2);

        userRepository.save(user1);

        userRepository.save(user2);



        // 포트폴리오1좋아요 15점
        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요
        redisService.likeCount(portFolio.getPortfolioId(), user1.getId(), "portFolio");  // 포트폴리오 유저 좋아요
        redisService.likeCount(portFolio.getPortfolioId(), user2.getId(), "portFolio");  // 포트폴리오 유저 좋아요

        //프토플리오2 좋아요 10점
        redisService.likeCount(portFolio1.getPortfolioId(), user1.getId(), "portFolio");  // 포트폴리오 유저 좋아요
        redisService.likeCount(portFolio1.getPortfolioId(), user2.getId(), "portFolio");  // 포트폴리오 유저 좋아요

        //포트폴리오3 조회수 1점
        redisService.viewCount(portFolio2.getPortfolioId(), user.getId(),"portFolio");


        // 게더링 점수 10점
        redisService.likeCount(gathering1.gatheringId(), user.getId(), "gathering");
        redisService.likeCount(gathering1.gatheringId(), user1.getId(), "gathering");

        // 게더링 점수 6점
        redisService.likeCount(gathering2.gatheringId(), user.getId(), "gathering");
        redisService.viewCount(gathering2.gatheringId(), user2.getId(), "gathering");

        redisService.categoryToDb("portFolio");
        redisService.categoryToDb("gathering");

        redisService.rankingCategory("portFolio");
        redisService.rankingCategory("gathering");

        List<PortFolioPopularResponse> portFolioPopularResponses = portFolioService.popularPortFolio(Optional.empty()).portfolioResponses();
        List<GatheringPopularResponse> gatheringPopularResponses = gatheringService.gatheringPopular(Optional.empty());

        Assertions.assertThat(portFolioPopularResponses.size()).isEqualTo(3);
        Assertions.assertThat(gatheringPopularResponses.size()).isEqualTo(2);
        Assertions.assertThat(portFolioPopularResponses.get(0).getScore()).isEqualTo(15);
        Assertions.assertThat(portFolioPopularResponses.get(1).getScore()).isEqualTo(10);
        Assertions.assertThat(gatheringPopularResponses.get(0).getScore()).isEqualTo(10);
        Assertions.assertThat(gatheringPopularResponses.get(1).getScore()).isEqualTo(6);

       //then
    }

    @Test
    @DisplayName("포트폴리오 redis를 사용한 조회수 동시성 처리")
    public void increment_hits() throws Exception {
       //given
        final int threadCount = 100;
        final ExecutorService executorService = Executors.newFixedThreadPool(32);
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        String key = constView + portFolio.getPortfolioId();


        //when
        for(int i = 0; i < threadCount; i++){
            executorService.submit(()-> {
                try{
                    redisTemplate.opsForValue().increment(key, 1L);
                }
                finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        RedisTemplate<String, Long> redisTemplate = redisService.getRedisTemplate();
        Long count = redisTemplate.opsForValue().get(key);

        Assertions.assertThat(count).isEqualTo(100);

       //then
    }

}
