package com.palettee.gathering.service;

import static org.junit.jupiter.api.Assertions.*;

import com.palettee.gathering.controller.dto.Request.*;
import com.palettee.gathering.controller.dto.Response.*;
import com.palettee.gathering.domain.Sort;
import com.palettee.gathering.domain.*;
import com.palettee.gathering.repository.*;
import com.palettee.global.exception.*;
import com.palettee.portfolio.controller.dto.response.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
import java.time.*;
import java.util.*;
import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.data.domain.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
class GatheringServiceTest {

    @Autowired
    private GatheringService gatheringService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private GatheringTagRepository gatheringTagRepository;

    @Autowired
    private GatheringImageRepository gatheringImageRepository;

    User savedUser;


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
    }


    @Test
    @DisplayName("유저의 게더링 생성")
    public void create_gathering() throws Exception {
       //given

        List<String> tagList = new ArrayList<>();

        tagList.add("tag1");
        tagList.add("tag2");

        List<String> imageList = new ArrayList<>();
        imageList.add("URL1");
        imageList.add("URL2");

        List<String> positions = new ArrayList<>();
        positions.add("개발자");
        positions.add("기획자");
        GatheringCommonRequest gatheringCreateRequest = new GatheringCommonRequest("프로젝트", "개발", "온라인", 3, "3개월", "2024-11-24", positions, tagList, "testUrl", "제목", "content", imageList);

        //when

        GatheringCommonResponse gathering = gatheringService.createGathering(gatheringCreateRequest, savedUser);


        Gathering gathering1 = gatheringRepository.findById(gathering.gatheringId()).get();
        List<GatheringTag> byGatheringId = gatheringTagRepository.findByGatheringId(gathering.gatheringId());

        List<GatheringImage> byGatheringId1 = gatheringImageRepository.findByGatheringId(gathering.gatheringId());


        //then

        Assertions.assertThat(gathering1.getSort()).isEqualTo(Sort.PROJECT);
        Assertions.assertThat(gathering1.getSubject()).isEqualTo(Subject.DEVELOP);
        Assertions.assertThat(byGatheringId.size()).isEqualTo(2);
        Assertions.assertThat(byGatheringId1.size()).isEqualTo(2);
    }


    @Test
    @DisplayName("유저의 게더링 생성시 설정된 값 이외의 값이 들어올때 ")
    public void gathering_bindingException() throws Exception {
        //given

        List<String> tagList = new ArrayList<>();

        tagList.add("tag1");
        tagList.add("tag2");

        List<String> imageList = new ArrayList<>();
        imageList.add("URL1");
        imageList.add("URL2");

        GatheringCommonRequest gatheringCreateRequest = new GatheringCommonRequest("테스트", "테스트", "온라인", 3, "3개월", "2024-11-24", null, tagList, "testUrl", "제목", "content",imageList);


       //then
        assertThrows(InvalidCategoryException.class, () -> {
            gatheringService.createGathering(gatheringCreateRequest, savedUser);
        });
    }


    @Test
    @DisplayName("게더링 전체 조회 페이징 NoOffSet을 활용한 조회")
    public void gathering_paging() throws Exception {
        List<String> tagList = new ArrayList<>();

        tagList.add("tag1");
        tagList.add("tag2");

        List<String> imageList = new ArrayList<>();
        imageList.add("URL1");
        imageList.add("URL2");

        List<String> positions = new ArrayList<>();
        positions.add("개발자");
        positions.add("기획자");

        for(int i = 0; i < 30; i++){
                GatheringCommonRequest gatheringCreateRequest = new GatheringCommonRequest("프로젝트", "개발", "온라인", 3, "3개월", "2024-11-24", positions, tagList, "testUrl", "제목", "content", imageList);

            gatheringService.createGathering(gatheringCreateRequest, savedUser);

        }

        //when

        CustomSliceResponse customSliceResponse = gatheringService.findAll("프로젝트", "개발","3개월", "온라인" ,null, "모집중", 3,null, PageRequest.of(0, 10), true);


        //then

        Assertions.assertThat(customSliceResponse.content().size()).isEqualTo(10);
        Assertions.assertThat(customSliceResponse.hasNext()).isEqualTo(true);

    }

    @Test
    @DisplayName("게더링 상세 조회")
    public void gathering_details() throws Exception {
        //given

        List<String> tagList = new ArrayList<>();

        tagList.add("tag1");
        tagList.add("tag2");


        List<String> imageList = new ArrayList<>();
        imageList.add("URL1");
        imageList.add("URL2");

        List<String> positions = new ArrayList<>();
        positions.add("개발자");
        positions.add("기획자");

        GatheringCommonRequest gatheringCreateRequest = new GatheringCommonRequest("프로젝트", "개발", "온라인", 3, "3개월", "2024-11-24", positions, tagList, "testUrl", "제목", "content", imageList);
        GatheringCommonResponse gathering = gatheringService.createGathering(gatheringCreateRequest, savedUser);

        //when

        GatheringDetailsResponse byDetails = gatheringService.findByDetails(gathering.gatheringId(), savedUser.getId());

        //then
        Assertions.assertThat(byDetails.sort()).isEqualTo(Sort.PROJECT.getSort());
    }


    @Test
    @DisplayName("게더링 업데이트")
    public void gathering_update() throws Exception {
        //given

        List<String> tagList = new ArrayList<>();

        tagList.add("tag1");
        tagList.add("tag2");


        List<String> imageList = new ArrayList<>();
        imageList.add("URL1");
        imageList.add("URL2");


        List<String> positions = new ArrayList<>();
        positions.add("개발자");
        positions.add("기획자");

        GatheringCommonRequest gatheringCreateRequest = new GatheringCommonRequest("프로젝트", "개발", "온라인", 3, "3개월", "2024-11-24", positions, tagList, "testUrl", "제목", "content",null);
        GatheringCommonResponse gathering = gatheringService.createGathering(gatheringCreateRequest, savedUser);



        List<String> updateList = new ArrayList<>();

        updateList.add("tag3");
        updateList.add("tag4");


        GatheringCommonRequest gatheringCreateRequest1 = new GatheringCommonRequest("스터디", "취미", "오프라인", 3, "3개월", "2024-11-24", null, updateList, "testUrl", "제목", "content", null);

        GatheringCommonResponse gatheringCreateResponse = gatheringService.updateGathering(gathering.gatheringId(), gatheringCreateRequest1, savedUser);

        Gathering gathering1 = gatheringRepository.findById(gatheringCreateResponse.gatheringId()).get();
        List<GatheringTag> byGatheringId = gatheringTagRepository.findByGatheringId(gathering.gatheringId());


        //then

        Assertions.assertThat(gathering1.getSort()).isEqualTo(Sort.STUDY);
        Assertions.assertThat(gathering1.getSubject()).isEqualTo(Subject.HOBBY);
        Assertions.assertThat(byGatheringId.size()).isEqualTo(2);
        Assertions.assertThat(byGatheringId.get(0).getContent()).isEqualTo("tag3");
    }



    @Test
    @DisplayName("게더링 모집완료 업데이트")
    public void gatheringStatus_upate() throws Exception {
       //given

        List<String> tagList = new ArrayList<>();

        tagList.add("tag1");
        tagList.add("tag2");

        List<String> imageList = new ArrayList<>();
        imageList.add("URL1");
        imageList.add("URL2");

        List<String> positions = new ArrayList<>();
        positions.add("개발자");
        positions.add("기획자");



        GatheringCommonRequest gatheringCreateRequest = new GatheringCommonRequest("프로젝트", "개발", "온라인", 3, "3개월", "2024-11-24", positions, tagList, "testUrl", "제목", "content",  imageList);
        GatheringCommonResponse gathering = gatheringService.createGathering(gatheringCreateRequest, savedUser);


       //when
        GatheringCommonResponse gatheringCommonResponse = gatheringService.updateStatusGathering(gathering.gatheringId(), savedUser);

        Gathering gathering1 = gatheringRepository.findById(gatheringCommonResponse.gatheringId()).get();

        //then
        Assertions.assertThat(gathering1.getStatus()).isEqualTo(Status.COMPLETE);
    }

//    @Test
//    @DisplayName("게더링 삭제 시에 태그와 이미지가 삭제되어야한다.")
//    public void gatheringDelete() throws Exception {
//       //given
//
//        List<String> tagList = new ArrayList<>();
//
//        tagList.add("tag1");
//        tagList.add("tag2");
//
//        List<String> imageList = new ArrayList<>();
//        imageList.add("URL1");
//        imageList.add("URL2");
//
//        GatheringCommonRequest gatheringCreateRequest = new GatheringCommonRequest("프로젝트", "개발", "온라인", 3, "3개월", "2024-11-24-09-30", "개발자", tagList, "testUrl", "제목", "content",  imageList);
//        GatheringCommonResponse gathering = gatheringService.createGathering(gatheringCreateRequest, savedUser);
//
//
//       //when
//
//        gatheringService.deleteGathering(gathering.gatheringId(), savedUser);
//
//        List<GatheringTag> byGatheringId = gatheringTagRepository.findByGatheringId(gathering.gatheringId());
//
//        List<GatheringImage> byGatheringId1 = gatheringImageRepository.findByGatheringId(gathering.gatheringId());
//
//
//       //then
//
//        Assertions.assertThat(byGatheringId.size()).isEqualTo(0);
//        Assertions.assertThat(byGatheringId1.size()).isEqualTo(0);
//    }

    @Test
    @DisplayName("게더링이 Ongoing 상태 이면서 시간이 만료된 애들 만료처리")
    public void expired_gatheringStatus() throws Exception {
       //given

        List<String> tagList = new ArrayList<>();

        tagList.add("tag1");
        tagList.add("tag2");

        List<String> imageList = new ArrayList<>();
        imageList.add("URL1");
        imageList.add("URL2");

        List<String> positions = new ArrayList<>();
        positions.add("개발자");
        positions.add("기획자");

        Gathering gathering = Gathering.builder()
                .user(savedUser)
                .sort(Sort.PROJECT)
                .gatheringImages(null)
                .gatheringTagList(null)
                .url("zz")
                .content("zz")
                .contact(Contact.OFFLINE)
                .period("zz")
                .subject(Subject.HOBBY)
                .personnel(4)
                .positions(null)
                .title("테스트")
                .deadLine(LocalDate.now()).build();

        Gathering save = gatheringRepository.save(gathering);
        gatheringRepository.save(gathering);
        gatheringRepository.save(gathering);

        //when

        Thread.sleep(1000); // 혹시 모르니 1초간 sleep

        gatheringService.updateGatheringStatus();


        List<Gathering> all = gatheringRepository.findAll();
        //then
        for (Gathering gatherings : all) {
            Assertions.assertThat(gatherings.getStatus()).isEqualTo(Status.EXPIRED);
        }
    }






}