package com.palettee.chat;

import com.palettee.chat.controller.dto.request.ChatImgUrlRequest;
import com.palettee.chat.controller.dto.request.ChatRequest;
import com.palettee.chat.controller.dto.response.ChatResponse;
import com.palettee.chat.repository.ChatRepository;
import com.palettee.chat.service.ChatService;
import com.palettee.chat_room.domain.ChatCategory;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.chat_room.repository.ChatRoomRepository;
import com.palettee.global.handler.exception.ChatContentNullException;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import com.palettee.user.domain.User;
import com.palettee.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

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
        savedUser = userRepository.save(new User("email", "imageUrl","name", "briefIntro", MajorJobGroup.DEVELOPER, MinorJobGroup.BACKEND));
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
