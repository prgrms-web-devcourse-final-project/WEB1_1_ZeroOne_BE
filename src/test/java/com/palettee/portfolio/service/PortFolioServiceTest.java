package com.palettee.portfolio.service;

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

import java.util.List;
import java.util.Optional;

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
    @DisplayName("포트폴리오 조회시 조회 수 증가")
    void portFolio_click() {
        // 총 3번 클릭
        portFolioService.clickPortFolio(portFolio.getPortfolioId());
        portFolioService.clickPortFolio(portFolio.getPortfolioId());
        portFolioService.clickPortFolio(portFolio.getPortfolioId());

        PortFolio findPortFolio = portFolioRepository.findById(portFolio.getPortfolioId()).orElseThrow();

        // then
        Assertions.assertThat(findPortFolio.getHits()).isEqualTo(3);
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
}
