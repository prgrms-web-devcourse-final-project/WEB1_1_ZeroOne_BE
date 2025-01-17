package com.palettee.gathering.service;

import com.palettee.gathering.controller.dto.Response.CustomSliceResponse;
import com.palettee.gathering.controller.dto.Response.GatheringPopularResponse;
import com.palettee.gathering.controller.dto.Response.GatheringResponse;
import com.palettee.gathering.domain.*;
import com.palettee.gathering.repository.GatheringRepository;
import com.palettee.global.cache.RedisWeightCache;
import com.palettee.global.redis.service.RedisService;
import com.palettee.likes.domain.LikeType;
import com.palettee.likes.domain.Likes;
import com.palettee.likes.repository.LikeRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.palettee.gathering.repository.GatheringRedisRepository.RedisConstKey_Gathering;
import static com.palettee.global.Const.LIKE_PREFIX;
import static com.palettee.global.Const.VIEW_PREFIX;

@SpringBootTest
public class GatheringRedisTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private GatheringService gatheringService;

    @Autowired
    private  RedisTemplate<String, GatheringResponse> gatheringRedisTemplate;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplateForTarget;

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisWeightCache redisWeightCache;

    private final String viewConst = VIEW_PREFIX + "gathering:";
    private final String likeConst = LIKE_PREFIX + "gathering:";


    private User savedUser;



    @BeforeEach
    void beforeEach() {
        savedUser = userRepository.save(
                User.builder()
                        .email("email")
                        .imageUrl("imageUrl")
                        .name("name")
                        .briefIntro("briefIntro")
                        .userRole(UserRole.USER)
                        .majorJobGroup(MajorJobGroup.DEVELOPER)
                        .minorJobGroup(MinorJobGroup.BACKEND)
                        .build()
        );
    }

    @AfterEach
    void tearDown(){
        userRepository.deleteAll();
        gatheringRepository.deleteAll();
        likeRepository.deleteAll();

        redisTemplate.getConnectionFactory().getConnection().flushAll();
        gatheringRedisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("게더링 레디스 첫번째 페이지 캐싱")
    public void 레디스를_사용한_첫번째_패이지_캐싱() throws Exception {
       //given
        for(int i =0; i < 20; i++){
            Gathering test = Gathering.builder()
                    .url("testUrl" + i)
                    .user(savedUser)
                    .title("testTitle" + i)
                    .content("zz")
                    .personnel(3)
                    .sort(Sort.CLUB)
                    .contact(Contact.OFFLINE)
                    .period("3개월")
                    .subject(Subject.DESIGN)
                    .deadLine(LocalDate.MAX)
                    .build();

            gatheringRepository.save(test);
        }

       //when
        gatheringService.findAll(null, null, null, null, null, "모집중", 0, null,   PageRequest.of(0, 10), Optional.empty(), true);

        Set<GatheringResponse> range = gatheringRedisTemplate.opsForZSet().range(RedisConstKey_Gathering, 0, -1);

        //then
        Assertions.assertThat(range.size()).isEqualTo(10);
    }

    @Test
    @DisplayName("첫번쨰 페이지 캐싱 좋아요 여부")
    public void 레디스를_활용한_첫번째페이지_캐싱_좋아요_여부() throws Exception {
        //given
        for(int i =0; i < 20; i++){
            Gathering test = Gathering.builder()
                    .url("testUrl" + i)
                    .user(savedUser)
                    .title("testTitle" + i)
                    .content("zz")
                    .personnel(3)
                    .sort(Sort.CLUB)
                    .contact(Contact.OFFLINE)
                    .period("3개월")
                    .subject(Subject.DESIGN)
                    .deadLine(LocalDate.MAX)
                    .build();

            gatheringRepository.save(test);

            Likes likes = Likes.builder()
                    .targetId(test.getId())
                    .user(savedUser)
                    .likeType(LikeType.GATHERING)
                    .build();
            likeRepository.save(likes);
        }

        User newuser = userRepository.save(
                User.builder()
                        .email("emailfd")
                        .imageUrl("imageUrl")
                        .name("name")
                        .briefIntro("briefIntro")
                        .userRole(UserRole.USER)
                        .majorJobGroup(MajorJobGroup.DEVELOPER)
                        .minorJobGroup(MinorJobGroup.BACKEND)
                        .build()
        );

        Gathering test = Gathering.builder()
                .url("testUrl" + 21)
                .user(newuser)
                .title("testTitle" + 21)
                .content("zz")
                .personnel(3)
                .sort(Sort.CLUB)
                .contact(Contact.OFFLINE)
                .period("3개월")
                .subject(Subject.DESIGN)
                .deadLine(LocalDate.MAX)
                .build();

        gatheringRepository.save(test);
        //when
        CustomSliceResponse customSliceResponse = gatheringService.findAll(null, null, null, null, null, "모집중", 0, null, PageRequest.of(0, 10), Optional.of(savedUser), true);


        List<GatheringResponse> content = customSliceResponse.content();
        System.out.println("size = {}"  + customSliceResponse.content().size());
        //then

        for(GatheringResponse gatheringResponse : content){
            System.out.println(gatheringResponse.isLiked());
        }
        Assertions.assertThat(content.get(0).isLiked()).isEqualTo(false);
        Assertions.assertThat(content.get(1).isLiked()).isEqualTo(true);
    }


    @Test
    @DisplayName("게더링_캐싱_조회후_db_반영")
    public void 게더링_조회후_DB반영() throws Exception {
        //given
        for(int i =0; i < 20; i++) {
            Gathering test = Gathering.builder()
                    .url("testUrl" + i)
                    .user(savedUser)
                    .title("testTitle" + i)
                    .content("zz")
                    .personnel(3)
                    .sort(Sort.CLUB)
                    .contact(Contact.OFFLINE)
                    .period("3개월")
                    .subject(Subject.DESIGN)
                    .deadLine(LocalDate.MAX)
                    .build();

            gatheringRepository.save(test);
        }

        User newuser = userRepository.save(
                User.builder()
                        .email("emailfd")
                        .imageUrl("imageUrl")
                        .name("name")
                        .briefIntro("briefIntro")
                        .userRole(UserRole.USER)
                        .majorJobGroup(MajorJobGroup.DEVELOPER)
                        .minorJobGroup(MinorJobGroup.BACKEND)
                        .build()
        );

        //when
        CustomSliceResponse customSliceResponse = gatheringService.findAll(null, null, null, null, null, "모집중", 0, null, PageRequest.of(0, 10), Optional.of(savedUser), true);


        gatheringService.findByDetails(customSliceResponse.content().get(0).getGatheringId(), savedUser.getId());
        gatheringService.findByDetails(customSliceResponse.content().get(0).getGatheringId(), newuser.getId());

        gatheringService.findByDetails(customSliceResponse.content().get(1).getGatheringId(), savedUser.getId());

        redisService.categoryToDb("gathering");

        Gathering gathering = gatheringRepository.findById(customSliceResponse.content().get(0).getGatheringId()).get();
        Gathering gathering1 = gatheringRepository.findById(customSliceResponse.content().get(1).getGatheringId()).get();


        //then
        Assertions.assertThat(gathering.getHits()).isEqualTo(2);
        Assertions.assertThat(gathering1.getHits()).isEqualTo(1);

    }

    @Test
    @DisplayName("게더링_좋아요후_DB반영")
    public void 게더링_좋아요_여부_반영() throws Exception {
        //given
        for(int i =0; i < 20; i++) {
            Gathering test = Gathering.builder()
                    .url("testUrl" + i)
                    .user(savedUser)
                    .title("testTitle" + i)
                    .content("zz")
                    .personnel(3)
                    .sort(Sort.CLUB)
                    .contact(Contact.OFFLINE)
                    .period("3개월")
                    .subject(Subject.DESIGN)
                    .deadLine(LocalDate.MAX)
                    .build();

            gatheringRepository.save(test);
        }

        User newuser = userRepository.save(
                User.builder()
                        .email("emailfd")
                        .imageUrl("imageUrl")
                        .name("name")
                        .briefIntro("briefIntro")
                        .userRole(UserRole.USER)
                        .majorJobGroup(MajorJobGroup.DEVELOPER)
                        .minorJobGroup(MinorJobGroup.BACKEND)
                        .build()
        );

        //when
        CustomSliceResponse customSliceResponse = gatheringService.findAll(null, null, null, null, null, "모집중", 0, null, PageRequest.of(0, 10), Optional.of(savedUser), true);

        List<GatheringResponse> content = customSliceResponse.content();

        gatheringService.createGatheringLike(content.get(0).getGatheringId(), savedUser);
        gatheringService.createGatheringLike(content.get(0).getGatheringId(),newuser);

        redisService.categoryToDb("gathering");

        long count = likeRepository.countByTargetId(content.get(0).getGatheringId());
        //then
        Assertions.assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("이미_좋아요_DB반영후_DB에서_좋아요삭제")
    public void 게더링_좋아요_DB에서_삭제() throws Exception {
        //given
        for(int i =0; i < 20; i++) {
            Gathering test = Gathering.builder()
                    .url("testUrl" + i)
                    .user(savedUser)
                    .title("testTitle" + i)
                    .content("zz")
                    .personnel(3)
                    .sort(Sort.CLUB)
                    .contact(Contact.OFFLINE)
                    .period("3개월")
                    .subject(Subject.DESIGN)
                    .deadLine(LocalDate.MAX)
                    .build();

            gatheringRepository.save(test);
        }

        User newuser = userRepository.save(
                User.builder()
                        .email("emailfd")
                        .imageUrl("imageUrl")
                        .name("name")
                        .briefIntro("briefIntro")
                        .userRole(UserRole.USER)
                        .majorJobGroup(MajorJobGroup.DEVELOPER)
                        .minorJobGroup(MinorJobGroup.BACKEND)
                        .build()
        );

        //when
        CustomSliceResponse customSliceResponse = gatheringService.findAll(null, null, null, null, null, "모집중", 0, null, PageRequest.of(0, 10), Optional.of(savedUser), true);

        List<GatheringResponse> content = customSliceResponse.content();

        gatheringService.createGatheringLike(content.get(0).getGatheringId(), savedUser);
        gatheringService.createGatheringLike(content.get(0).getGatheringId(),newuser);

        redisService.categoryToDb("gathering");


        gatheringService.createGatheringLike(content.get(0).getGatheringId(), savedUser);
        long count = likeRepository.countByTargetId(content.get(0).getGatheringId());
        //then
        Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("게더링 가중치 기록 확인")
    public void 게더링_가중치_기록() throws Exception {
        //given
        for(int i =0; i < 20; i++) {
            Gathering test = Gathering.builder()
                    .url("testUrl" + i)
                    .user(savedUser)
                    .title("testTitle" + i)
                    .content("zz")
                    .personnel(3)
                    .sort(Sort.CLUB)
                    .contact(Contact.OFFLINE)
                    .period("3개월")
                    .subject(Subject.DESIGN)
                    .deadLine(LocalDate.MAX)
                    .build();

            gatheringRepository.save(test);
        }

        User newuser = userRepository.save(
                User.builder()
                        .email("emailfd")
                        .imageUrl("imageUrl")
                        .name("name")
                        .briefIntro("briefIntro")
                        .userRole(UserRole.USER)
                        .majorJobGroup(MajorJobGroup.DEVELOPER)
                        .minorJobGroup(MinorJobGroup.BACKEND)
                        .build()
        );

        //when
        CustomSliceResponse customSliceResponse = gatheringService.findAll(null, null, null, null, null, "모집중", 0, null, PageRequest.of(0, 10), Optional.of(savedUser), true);


        gatheringService.findByDetails(customSliceResponse.content().get(0).getGatheringId(), savedUser.getId());
        gatheringService.findByDetails(customSliceResponse.content().get(0).getGatheringId(), newuser.getId());

        gatheringService.findByDetails(customSliceResponse.content().get(1).getGatheringId(), savedUser.getId());

        gatheringService.createGatheringLike(customSliceResponse.content().get(0).getGatheringId(), savedUser);
        gatheringService.createGatheringLike(customSliceResponse.content().get(0).getGatheringId(),newuser);

        redisService.categoryToDb("gathering");


        Map<Long, Long> viewCache = redisWeightCache.getCache(viewConst);
        Map<Long, Long> likeCache = redisWeightCache.getCache(likeConst);


        Long viewCount = viewCache.get(customSliceResponse.content().get(0).getGatheringId());
        Long likeCount = likeCache.get(customSliceResponse.content().get(0).getGatheringId());



        //then
        Assertions.assertThat(viewCount).isEqualTo(2);
        Assertions.assertThat(likeCount).isEqualTo(10);
    }

    @Test
    @DisplayName("인기 게더링 조회")
    public void 인기_게더링_조회() throws Exception {
        //given
        for(int i =0; i < 20; i++) {
            Gathering test = Gathering.builder()
                    .url("testUrl" + i)
                    .user(savedUser)
                    .title("testTitle" + i)
                    .content("zz")
                    .personnel(3)
                    .sort(Sort.CLUB)
                    .contact(Contact.OFFLINE)
                    .period("3개월")
                    .subject(Subject.DESIGN)
                    .deadLine(LocalDate.MAX)
                    .build();

            gatheringRepository.save(test);
        }

        User newuser = userRepository.save(
                User.builder()
                        .email("emailfd")
                        .imageUrl("imageUrl")
                        .name("name")
                        .briefIntro("briefIntro")
                        .userRole(UserRole.USER)
                        .majorJobGroup(MajorJobGroup.DEVELOPER)
                        .minorJobGroup(MinorJobGroup.BACKEND)
                        .build()
        );

        //when
        CustomSliceResponse customSliceResponse = gatheringService.findAll(null, null, null, null, null, "모집중", 0, null, PageRequest.of(0, 10), Optional.of(savedUser), true);

        gatheringService.findByDetails(customSliceResponse.content().get(0).getGatheringId(), savedUser.getId());
        gatheringService.findByDetails(customSliceResponse.content().get(0).getGatheringId(), newuser.getId());

        gatheringService.findByDetails(customSliceResponse.content().get(1).getGatheringId(), savedUser.getId());

        gatheringService.createGatheringLike(customSliceResponse.content().get(0).getGatheringId(), savedUser);
        gatheringService.createGatheringLike(customSliceResponse.content().get(0).getGatheringId(),newuser);

        gatheringService.createGatheringLike(customSliceResponse.content().get(1).getGatheringId(), savedUser);
        gatheringService.createGatheringLike(customSliceResponse.content().get(1).getGatheringId(), newuser);

        gatheringService.findByDetails(customSliceResponse.content().get(2).getGatheringId(), savedUser.getId());
        gatheringService.findByDetails(customSliceResponse.content().get(3).getGatheringId(), savedUser.getId());
        gatheringService.findByDetails(customSliceResponse.content().get(4).getGatheringId(), savedUser.getId());

        redisService.categoryToDb("gathering");

        redisService.rankingCategory("gathering");

        List<GatheringPopularResponse> gatheringPopularResponses = gatheringService.gatheringPopular(Optional.empty());

        Optional<GatheringPopularResponse> first = gatheringPopularResponses.stream().
                filter(gatheringPopularResponse -> gatheringPopularResponse.getGatheringId().equals(customSliceResponse.content().get(0).getGatheringId()))
                .findFirst();

        //then
        Assertions.assertThat(gatheringPopularResponses.size()).isEqualTo(4);
        Assertions.assertThat(first.get().getScore()).isEqualTo(12.0);

    }

}
