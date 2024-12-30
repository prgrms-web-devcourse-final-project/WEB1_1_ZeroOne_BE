package com.palettee.portfolio.service;

import static com.palettee.global.Const.*;

import com.palettee.gathering.controller.dto.Response.CustomSliceResponse;
import com.palettee.global.cache.*;
import com.palettee.global.redis.service.*;
import com.palettee.likes.domain.*;
import com.palettee.likes.repository.*;
import com.palettee.portfolio.controller.dto.response.*;
import com.palettee.portfolio.domain.*;
import com.palettee.portfolio.repository.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
import java.util.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.*;

@SpringBootTest
class PortFolioServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PortFolioServiceTest.class);
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
                .userRole(UserRole.USER)
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

//    @Test
//    @DisplayName("포트폴리오 전체조회 무한스크롤 처리")
//    void portfolio_pageNation() {
//        // given
//        for (int i = 0; i < 20; i++) {
//            PortFolio portFolio = PortFolio.builder()
//                    .user(user)
//                    .url("테스트테스트1")
//                    .build();
//            portFolioRepository.save(portFolio);
//        }
//
//        // when
//        List<PortFolio> all = portFolioRepository.findAll();
//        System.out.println(all.size());
//
//        PageRequest pageRequest = PageRequest.of(0, 10);
//        CustomOffSetResponse results = portFolioService.findAllPortFolio(
//                pageRequest,
//                MajorJobGroup.DEVELOPER.getMajorGroup(),
//                MinorJobGroup.BACKEND.getMinorJobGroup(),
//                "popularlity"
//                ,true
//        );
//        System.out.println(results.hasNext());
//
//        // then
//        Assertions.assertThat(results.pageSize()).isEqualTo(10);
//        Assertions.assertThat(results.hasNext()).isEqualTo(true);
//    }

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
        CustomPortFolioResponse customSliceResponse = portFolioService.findListPortFolio(pageRequest, user.getId(), null);

        // then
        Assertions.assertThat(customSliceResponse.content().size()).isEqualTo(10);
        Assertions.assertThat(customSliceResponse.hasNext()).isEqualTo(true);
    }

//    @Test
//    @DisplayName("포트폴리오 좋아요 생성")
//    void portFolio_Like_Create() {
//        // given
//        PortFolioLikeResponse portFolioLike = portFolioService.createPortFolioLike(portFolio.getPortfolioId(), user);
//
//        // when
//        Likes likes = likeRepository.findById(portFolioLike.portFolioId()).orElseThrow();
//
//        // then
//        Assertions.assertThat(likes.getLikeType()).isEqualTo(LikeType.PORTFOLIO);
//    }
//
//    @Test
//    @DisplayName("포트 폴리오 좋아요 취소")
//    public void portFolio_Like_Cancel() throws Exception {
//       //given
//        PortFolioLikeResponse portFolioLike = portFolioService.createPortFolioLike(portFolio.getPortfolioId(), user);
//
//       //when
//        PortFolioLikeResponse portFolioLik1 = portFolioService.createPortFolioLike(portFolio.getPortfolioId(), user);
//
//        Optional<Likes> findByLikes = likeRepository.findById(portFolioLike.portFolioId());
//
//        //then
//        Assertions.assertThat(findByLikes.isPresent()).isEqualTo(false);
//    }

    @Test
    @DisplayName("Redis를 사용한 포트폴리오 중복 조회수 증가 및 DB 반영 테스트")
    public void portFolio_hits_redis() throws Exception {
       //given

        RedisTemplate<String, Long> redisTemplate = redisService.getRedisTemplate();


        for(int i =0 ; i < 5; i++){
            redisService.viewCount(portFolio.getPortfolioId(), user.getId(),"portFolio");
        }

       //when

        redisService.viewRedisToDB(VIEW_PREFIX + "portFolio: ");

        Map<String, Long> localCache = memoryCache.getLocalCache();

        PortFolio portFolio1 = portFolioRepository.findById(portFolio.getPortfolioId()).get();



        Long remainCount = redisTemplate.opsForValue().get(VIEW_PREFIX + "portFolio" + ": " + portFolio.getPortfolioId());

        Long cacheCount = localCache.get(VIEW_PREFIX + "portFolio" + ": " + portFolio.getPortfolioId());

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

        //when

        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요
        redisService.likeCount(portFolio.getPortfolioId(), user1.getId(), "portFolio");  // 포트폴리오 유저 좋아요
        redisService.likeCount(portFolio.getPortfolioId(), user2.getId(), "portFolio");  // 포트폴리오 유저 좋아요


        redisService.likeRedisToDB( LIKE_PREFIX + "portFolio: ", "portFolio" );

        Map<String, Long> localCache = memoryCache.getLocalCache();

        Long aw = localCache.get(LIKE_PREFIX + "portFolio: " + portFolio.getPortfolioId());


        List<Likes> byTargetId = likeRepository.findByTargetId(portFolio.getPortfolioId());

        //then
        Assertions.assertThat(byTargetId).hasSize(3);
        Assertions.assertThat(aw).isEqualTo(15);


    }


    @Test
    @DisplayName("인기 포폴 RedisZset을 활용한 누적 점수 합산 후 순위 메기기")
    public void reids_score() throws Exception {
        //given
        RedisTemplate<String, Long> redisTemplate = redisService.getRedisTemplate();


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

        //when

        // 좋아요 총 3개 점수 15점
        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요
        redisService.likeCount(portFolio.getPortfolioId(), user1.getId(), "portFolio");  // 포트폴리오 유저 좋아요
        redisService.likeCount(portFolio.getPortfolioId(), user2.getId(), "portFolio");  // 포트폴리오 유저 좋아요

        // 중복 조회 이므로 1
        for(int i =0 ; i < 5; i++){
            redisService.viewCount(portFolio.getPortfolioId(), user.getId(),"portFolio");
        }

        redisService.likeRedisToDB( LIKE_PREFIX + "portFolio: ", "portFolio" );
        redisService.viewRedisToDB(VIEW_PREFIX + "portFolio: ");

        redisService.rankingCategory("portFolio");

        Long portFolioRanking = redisTemplate.opsForZSet().size("portFolio_Ranking");
        Double score = redisTemplate.opsForZSet().score("portFolio_Ranking", portFolio.getPortfolioId());

        System.out.println("portFolioId ="+ portFolioRanking);


        //then
//        Assertions.assertThat(size).isEqualTo(1);
        Assertions.assertThat(score).isEqualTo(16.0);
    }

//
//    @Test
//    @DisplayName("인기 포트폴리오 캐시 ")
//    public void popularity() throws Exception {
//        User user1 = User.builder()
//                .imageUrl("image")
//                .email("hellod")
//                .name("테스트")
//                .briefIntro("안녕하세요")
//                .majorJobGroup(MajorJobGroup.DEVELOPER)
//                .minorJobGroup(MinorJobGroup.BACKEND)
//                .build();
//
//        User user2 = User.builder()
//                .imageUrl("image")
//                .email("hellos")
//                .name("테스트")
//                .briefIntro("안녕하세요")
//                .majorJobGroup(MajorJobGroup.DEVELOPER)
//                .minorJobGroup(MinorJobGroup.BACKEND)
//                .build();
//
//        userRepository.save(user1);
//
//        userRepository.save(user2);
//
//        PortFolio portFolio1 = PortFolio.builder()
//                .user(user)
//                .url("테스트테스트")
//                .build();
//        portFolioRepository.save(portFolio1);
//
//        //when
//
//        // portFolio 점수 20 점 포트폴리오 1 점수 15점
//        redisService.likeCount(portFolio.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요
//        redisService.likeCount(portFolio.getPortfolioId(), user1.getId(), "portFolio");  // 포트폴리오 유저 좋아요
//        redisService.likeCount(portFolio.getPortfolioId(), user2.getId(), "portFolio");  // 포트폴리오 유저 좋아요
//
//        redisService.likeCount(portFolio1.getPortfolioId(), user.getId(), "portFolio");  // 포트폴리오 유저 좋아요
//        redisService.likeCount(portFolio1.getPortfolioId(), user1.getId(), "portFolio");  // 포트폴리오 유저 좋아요
//
//
//        for(int i =0 ; i < 5; i++){
//            redisService.viewCount(portFolio.getPortfolioId(),user.getId(), "portFolio");
//            redisService.viewCount(portFolio1.getPortfolioId(), user1.getId(),"portFolio");
//        }
//
//        redisService.likeRedisToDB( LIKE_PREFIX + "portFolio: ", "portFolio" ); //DB반영
//        redisService.viewRedisToDB(VIEW_PREFIX + "portFolio: "); // DB 반영
//
//        redisService.rankingCategory("portFolio"); // 누적 점수 반영
//
//        PortFolioWrapper portFolioWrapper = portFolioService.popularPortFolio();// 유명 포트폴리오 조회
//
//        List<PortFolioPopularResponse> portFolioResponses = portFolioWrapper.portfolioResponses();
//
//
//        //then
//        Assertions.assertThat(portFolioResponses.get(0).portFolioId()).isEqualTo(portFolio.getPortfolioId());
//        Assertions.assertThat(portFolioResponses.get(1).portFolioId()).isEqualTo(portFolio1.getPortfolioId());
//    }
}
