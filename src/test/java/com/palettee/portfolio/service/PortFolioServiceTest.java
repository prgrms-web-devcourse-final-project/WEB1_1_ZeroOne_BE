package com.palettee.portfolio.service;

import com.palettee.portfolio.controller.dto.PortFolioResponseDTO;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.portfolio.domain.QPortFolio;
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

@SpringBootTest
class PortFolioServiceTest {

    @Autowired
    private PortFolioService portFolioService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PortFolioRepository portFolioRepository;

    User user;

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

        PortFolio portFolio = PortFolio.builder()
                .user(user)
                .url("테스트테스트")
                .build();


        portFolioRepository.save(portFolio);
    }



    @AfterEach
    void tearDown() {
        portFolioRepository.deleteAll();
        userRepository.deleteAll();
    }



    @Test
    @DisplayName("포트폴리오 전체조회 무한스크롤 처리 ")
    public void portfolio_pageNation() throws Exception {
       //given
       for(int i = 0; i < 20; i++){

           PortFolio portFolio = PortFolio.builder()
                   .user(user)
                   .url("테스트테스트1")
                   .build();

           portFolioRepository.save(portFolio);
       }
       //when

        List<PortFolio> all = portFolioRepository.findAll();

        System.out.println(all.size());

        PageRequest pageRequest = PageRequest.of(0, 10);

        Slice<PortFolioResponseDTO> results = portFolioService.findAllPortFolio(pageRequest, MajorJobGroup.DEVELOPER, MinorJobGroup.BACKEND, "popularlity");

        //then
        Assertions.assertThat(results.getSize()).isEqualTo(10);
        Assertions.assertThat(results.hasNext()).isEqualTo(true);
    }

}