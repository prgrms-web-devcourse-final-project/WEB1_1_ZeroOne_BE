package com.palettee.chat;

import static org.assertj.core.api.Assertions.*;

import com.palettee.chat.controller.dto.request.*;
import com.palettee.chat.controller.dto.response.*;
import com.palettee.chat.repository.*;
import com.palettee.chat.service.*;
import com.palettee.chat_room.domain.*;
import com.palettee.chat_room.repository.*;
import com.palettee.global.handler.exception.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;

@SpringBootTest
@Transactional
public class ChatServiceTest {

    @Autowired ChatService chatService;
    @Autowired UserRepository userRepository;
    @Autowired ChatRoomRepository chatRoomRepository;
    @Autowired ChatRepository chatRepository;

    private User savedUser;
    private ChatRoom savedChatRoom;

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
        savedChatRoom = chatRoomRepository.save(new ChatRoom(ChatCategory.COFFEE_CHAT));
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
        chatRoomRepository.deleteAll();
        chatRepository.deleteAll();
    }

    @Test
    @DisplayName("채팅 저장, 채팅 이미지 null")
    void saveChat() {
        // given
        String userEmail = savedUser.getEmail();
        Long chatRoomId = savedChatRoom.getId();
        ChatRequest chatRequest = new ChatRequest("Hi", null);

        // when
        ChatResponse chatResponse = chatService.saveChat(userEmail, chatRoomId, chatRequest);

        // then
        assertThat(chatResponse.getChatId()).isNotNull();
        assertThat(chatResponse.getChatRoomId()).isNotNull();
        assertThat(chatResponse.getEmail()).isEqualTo(userEmail);
        assertThat(chatResponse.getProfileImg()).isEqualTo("imageUrl");
        assertThat(chatResponse.getContent()).isEqualTo("Hi");
        assertThat(chatResponse.getImgUrls()).isNull();
    }

    @Test
    @DisplayName("채팅 이미지 저장, 채팅 null")
    void saveChatImage() {
        // given
        String userEmail = savedUser.getEmail();
        Long chatRoomId = savedChatRoom.getId();
        List<ChatImgUrlRequest> urls = new ArrayList<>();
        ChatRequest chatImgRequest = new ChatRequest(null, urls);

        urls.add(new ChatImgUrlRequest("aaa"));
        urls.add(new ChatImgUrlRequest("bbb"));
        urls.add(new ChatImgUrlRequest("ccc"));

        // when
        ChatResponse chatResponse = chatService.saveChat(userEmail, chatRoomId, chatImgRequest);

        // then
        assertThat(chatResponse.getChatId()).isNotNull();
        assertThat(chatResponse.getChatRoomId()).isNotNull();
        assertThat(chatResponse.getEmail()).isEqualTo(userEmail);
        assertThat(chatResponse.getProfileImg()).isEqualTo("imageUrl");
        assertThat(chatResponse.getImgUrls().size()).isEqualTo(3);
        assertThat(chatResponse.getContent()).isNull();
    }

    @Test
    @DisplayName("채팅 이미지 저장, 채팅 null, 채팅 이미지 url empty")
    void ImageUrlEmpty() {
        // given
        String userEmail = savedUser.getEmail();
        Long chatRoomId = savedChatRoom.getId();
        List<ChatImgUrlRequest> urls = new ArrayList<>();
        ChatRequest chatImgRequest = new ChatRequest(null, urls);

        // then
        assertThatThrownBy(() -> chatService.saveChat(userEmail, chatRoomId, chatImgRequest))
                .isInstanceOf(ChatContentNullException.class);
    }

    @Test
    @DisplayName("채팅 이미지 저장, 채팅 저장")
    void saveChatAndChatImage() {
        // given
        String userEmail = savedUser.getEmail();
        Long chatRoomId = savedChatRoom.getId();
        List<ChatImgUrlRequest> urls = new ArrayList<>();
        ChatRequest chatImgRequest = new ChatRequest("Hi", urls);

        urls.add(new ChatImgUrlRequest("aaa"));
        urls.add(new ChatImgUrlRequest("bbb"));
        urls.add(new ChatImgUrlRequest("ccc"));

        // when
        ChatResponse chatResponse = chatService.saveChat(userEmail, chatRoomId, chatImgRequest);

        // then
        assertThat(chatResponse.getChatId()).isNotNull();
        assertThat(chatResponse.getChatRoomId()).isNotNull();
        assertThat(chatResponse.getEmail()).isEqualTo(userEmail);
        assertThat(chatResponse.getProfileImg()).isEqualTo("imageUrl");
        assertThat(chatResponse.getImgUrls().size()).isEqualTo(3);
        assertThat(chatResponse.getContent()).isEqualTo("Hi");
    }

    @Test
    @DisplayName("채팅 이미지 저장, 채팅 저장")
    void saveChatAndChatImageAndImgUrlEmpty() {
        // given
        String userEmail = savedUser.getEmail();
        Long chatRoomId = savedChatRoom.getId();
        List<ChatImgUrlRequest> urls = new ArrayList<>();
        ChatRequest chatImgRequest = new ChatRequest("Hi", urls);

        // when
        ChatResponse chatResponse = chatService.saveChat(userEmail, chatRoomId, chatImgRequest);

        // then
        assertThat(chatResponse.getChatId()).isNotNull();
        assertThat(chatResponse.getChatRoomId()).isNotNull();
        assertThat(chatResponse.getEmail()).isEqualTo(userEmail);
        assertThat(chatResponse.getProfileImg()).isEqualTo("imageUrl");
        assertThat(chatResponse.getImgUrls()).isNull();
        assertThat(chatResponse.getContent()).isEqualTo("Hi");
    }

    @Test
    @DisplayName("채팅 null, 채팅 이미지 null")
    void allNull() {
        // given
        String userEmail = savedUser.getEmail();
        Long chatRoomId = savedChatRoom.getId();
        ChatRequest chatImgRequest = new ChatRequest(null, null);

        // then
        assertThatThrownBy(() -> chatService.saveChat(userEmail, chatRoomId, chatImgRequest))
                .isInstanceOf(ChatContentNullException.class);
    }
}
