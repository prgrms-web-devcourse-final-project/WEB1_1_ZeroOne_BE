package com.palettee.chat;

import com.palettee.chat.controller.dto.request.ChatRequest;
import com.palettee.chat.controller.dto.response.ChatCustomResponse;
import com.palettee.chat.controller.dto.response.ChatImgUrl;
import com.palettee.chat.controller.dto.response.ChatResponse;
import com.palettee.chat.domain.Chat;
import com.palettee.chat.repository.ChatRepository;
import com.palettee.chat.service.ChatRedisService;
import com.palettee.chat_room.domain.ChatCategory;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.chat_room.repository.ChatRoomRepository;
import com.palettee.global.redis.utils.TypeConverter;
import com.palettee.user.domain.*;
import com.palettee.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.palettee.global.Const.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
public class ChatRedisServiceTest {

    @Autowired UserRepository userRepository;
    @Autowired ChatRoomRepository chatRoomRepository;
    @Autowired ChatRepository chatRepository;
    @Autowired ChatRedisService chatRedisService;
    @Qualifier("chatRedisTemplate") @Autowired RedisTemplate<String, ChatResponse> redisTemplate;

    private ZSetOperations<String, ChatResponse> zSetOperations;
    private User savedUser;
    private ChatRoom savedChatRoom;

    @BeforeEach
    void beforeEach() {
        zSetOperations = redisTemplate.opsForZSet();

        savedUser = userRepository.save(
                User.builder()
                        .minorJobGroup(MinorJobGroup.ANALYSIS)
                        .majorJobGroup(MajorJobGroup.DESIGN)
                        .division(Division.ETC)
                        .jobTitle("aa")
                        .briefIntro("briefIntro")
                        .name("name")
                        .imageUrl("imageUrl")
                        .email("email")
                        .userRole(UserRole.USER)
                        .oauthIdentity("oauthIdentity")
                        .id(1L)
                        .build());
        savedChatRoom = chatRoomRepository.save(new ChatRoom(ChatCategory.COFFEE_CHAT));
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
        chatRoomRepository.deleteAll();
        chatRepository.deleteAll();

        redisTemplate.delete(CHATROOM_KEY_PREFIX + String.valueOf(savedChatRoom.getId()));
    }

    @Test
    @DisplayName("채팅 저장")
    void saveChat() {
        // given
        String userEmail = savedUser.getEmail();
        Long chatRoomId = savedChatRoom.getId();
        List<ChatImgUrl> urls = new ArrayList<>();
        ChatRequest chatRequest = new ChatRequest("Hi", urls);

        // when
        ChatResponse chatResponse = chatRedisService.addChat(userEmail, chatRoomId, chatRequest);
        Double sendAt = TypeConverter.LocalDateTimeToDouble(chatResponse.getSendAt());
        List<ChatResponse> results = zSetOperations.rangeByScore(CHATROOM_KEY_PREFIX + String.valueOf(chatResponse.getChatRoomId()), sendAt, sendAt).stream().toList();

        // then
        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0).getContent()).isEqualTo("Hi");
    }

    @Test
    @DisplayName("redis에서 조회 && DB에 데이터 X")
    void getChatsSituation1() {
        //given
        String userEmail = savedUser.getEmail();
        Long chatRoomId = savedChatRoom.getId();

        List<ChatImgUrl> urls1 = new ArrayList<>();
        List<ChatImgUrl> urls2 = new ArrayList<>();
        List<ChatImgUrl> urls3 = new ArrayList<>();

        ChatRequest chatRequest1 = new ChatRequest("Hi1", urls1);
        ChatRequest chatRequest2 = new ChatRequest("Hi2", urls2);
        ChatRequest chatRequest3 = new ChatRequest("Hi3", urls3);

        chatRedisService.addChat(userEmail, chatRoomId, chatRequest1);
        chatRedisService.addChat(userEmail, chatRoomId, chatRequest2);
        chatRedisService.addChat(userEmail, chatRoomId, chatRequest3);

        //when
        ChatCustomResponse chatCustomResponse = chatRedisService.getChats(chatRoomId, 3, null);

        //then
        assertThat(chatCustomResponse.getChats().size()).isEqualTo(3);
        assertThat(chatCustomResponse.isHasNext()).isFalse();
        assertThat(chatCustomResponse.getLastSendAt()).isNull();
    }

    @Test
    @DisplayName("redis에 전혀 없고 DB에만 있을 때")
    void getChatsSituation2() {
        //given
        Chat chat1 = makeChat("Hi1");
        Chat chat2 = makeChat("Hi2");
        Chat chat3 = makeChat("Hi3");

        List<Chat> chats = new ArrayList<>();
        chats.add(chat1);
        chats.add(chat2);
        chats.add(chat3);

        chatRepository.saveAll(chats);

        //when
        ChatCustomResponse chatCustomResponse = chatRedisService.getChats(savedChatRoom.getId(), 3, null);

        //then
        assertThat(chatCustomResponse.getChats().size()).isEqualTo(3);
        assertThat(chatCustomResponse.isHasNext()).isFalse();
        assertThat(chatCustomResponse.getLastSendAt()).isNull();
    }

    @Test
    @DisplayName("redis에 1개 DB에 2개 있을 때")
    void getChatsSituation3() {
        //given
        Chat chat1 = makeChat("Hi1");
        Chat chat2 = makeChat("Hi2");

        List<Chat> chats = new ArrayList<>();
        chats.add(chat1);
        chats.add(chat2);

        chatRepository.saveAll(chats);

        List<ChatImgUrl> urls1 = new ArrayList<>();
        ChatRequest chatRequest1 = new ChatRequest("Hi3", urls1);

        chatRedisService.addChat(savedUser.getEmail(), savedChatRoom.getId(), chatRequest1);

        //when
        ChatCustomResponse chatCustomResponse = chatRedisService.getChats(savedChatRoom.getId(), 3, null);

        //then
        assertThat(chatCustomResponse.getChats().size()).isEqualTo(3);
        assertThat(chatCustomResponse.isHasNext()).isFalse();
        assertThat(chatCustomResponse.getLastSendAt()).isNull();
    }

    @Test
    @DisplayName("redis에 1개 DB에 3개 있을 때")
    void getChatsSituation4() {
        //given
        Chat chat1 = makeChat("Hi1");
        Chat chat2 = makeChat("Hi2");
        Chat chat3 = makeChat("Hi3");

        List<Chat> chats = new ArrayList<>();
        chats.add(chat1);
        chats.add(chat2);
        chats.add(chat3);

        chatRepository.saveAll(chats);

        List<ChatImgUrl> urls1 = new ArrayList<>();
        ChatRequest chatRequest1 = new ChatRequest("Hi", urls1);

        chatRedisService.addChat(savedUser.getEmail(), savedChatRoom.getId(), chatRequest1);

        //when
        ChatCustomResponse chatCustomResponse = chatRedisService.getChats(savedChatRoom.getId(), 3, null);

        //then
        assertThat(chatCustomResponse.getChats().size()).isEqualTo(3);
        assertThat(chatCustomResponse.isHasNext()).isTrue();
        assertThat(chatCustomResponse.getLastSendAt()).isEqualTo(chat2.getSendAt());
    }

    @Test
    @DisplayName("redis 데이터 4개")
    void getChatsSituation5() {
        //given
        String userEmail = savedUser.getEmail();
        Long chatRoomId = savedChatRoom.getId();

        List<ChatImgUrl> urls1 = new ArrayList<>();
        List<ChatImgUrl> urls2 = new ArrayList<>();
        List<ChatImgUrl> urls3 = new ArrayList<>();
        List<ChatImgUrl> urls4 = new ArrayList<>();

        ChatRequest chatRequest1 = new ChatRequest("Hi1", urls1);
        ChatRequest chatRequest2 = new ChatRequest("Hi2", urls2);
        ChatRequest chatRequest3 = new ChatRequest("Hi3", urls3);
        ChatRequest chatRequest4 = new ChatRequest("Hi4", urls4);

        chatRedisService.addChat(userEmail, chatRoomId, chatRequest1);
        ChatResponse chatResponse = chatRedisService.addChat(userEmail, chatRoomId, chatRequest2);
        chatRedisService.addChat(userEmail, chatRoomId, chatRequest3);
        chatRedisService.addChat(userEmail, chatRoomId, chatRequest4);

        //when
        ChatCustomResponse chatCustomResponse = chatRedisService.getChats(chatRoomId, 3, null);

        //then
        assertThat(chatCustomResponse.getChats().size()).isEqualTo(3);
        assertThat(chatCustomResponse.isHasNext()).isTrue();
        assertThat(chatCustomResponse.getLastSendAt()).isEqualTo(chatResponse.getSendAt());
    }

    private Chat makeChat(String content) {
        return Chat.builder()
                .id(UUID.randomUUID().toString())
                .user(savedUser)
                .chatRoom(savedChatRoom)
                .content(content)
                .sendAt(LocalDateTime.now())
                .build();
    }
}
