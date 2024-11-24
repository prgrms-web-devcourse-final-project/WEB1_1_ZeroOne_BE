package com.palettee.gathering.service;

import com.palettee.gathering.controller.dto.Request.GatheringCreateRequest;
import com.palettee.gathering.controller.dto.Response.GatheringCreateResponse;
import com.palettee.gathering.domain.Gathering;
import com.palettee.gathering.domain.GatheringTag;
import com.palettee.gathering.domain.Sort;
import com.palettee.gathering.domain.Subject;
import com.palettee.gathering.repository.GatheringRepository;
import com.palettee.gathering.repository.GatheringTagRepository;
import com.palettee.global.exception.InvalidCategoryException;
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

    User savedUser;


    @BeforeEach
    void beforeEach() {
        savedUser = userRepository.save(new User("email", "imageUrl","name", "briefIntro", MajorJobGroup.DEVELOPER, MinorJobGroup.BACKEND));
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

        GatheringCreateRequest gatheringCreateRequest = new GatheringCreateRequest("프로젝트", "개발", "온라인", 3, "3개월", "2024-11-24", "개발자", tagList, "testUrl", "제목", "content");

        //when

        GatheringCreateResponse gathering = gatheringService.createGathering(gatheringCreateRequest, savedUser);


        Gathering gathering1 = gatheringRepository.findById(gathering.gatheringId()).get();
        List<GatheringTag> byGatheringId = gatheringTagRepository.findByGatheringId(gathering.gatheringId());

        //then

        Assertions.assertThat(gathering1.getSort()).isEqualTo(Sort.PROJECT);
        Assertions.assertThat(gathering1.getSubject()).isEqualTo(Subject.DEVELOP);
        Assertions.assertThat(byGatheringId.size()).isEqualTo(2);
    }


    @Test
    @DisplayName("유저의 게더링 생성시 설정된 값 이외의 값이 들어올때 ")
    public void gathering_bindingException() throws Exception {
        //given

        List<String> tagList = new ArrayList<>();

        tagList.add("tag1");
        tagList.add("tag2");

        GatheringCreateRequest gatheringCreateRequest = new GatheringCreateRequest("테스트", "테스트", "온라인", 3, "3개월", "2024-11-24", "개발자", tagList, "testUrl", "제목", "content");


       //then
        assertThrows(InvalidCategoryException.class, () -> {
            gatheringService.createGathering(gatheringCreateRequest, savedUser);
        });
    }







}