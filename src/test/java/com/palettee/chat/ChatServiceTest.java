package com.palettee.chat;

import com.palettee.chat.controller.dto.request.ChatImgRequest;
import com.palettee.chat.controller.dto.request.ChatImgUrlRequest;
import com.palettee.chat.controller.dto.request.ChatRequest;
import com.palettee.chat.controller.dto.response.ChatImgResponse;
import com.palettee.chat.controller.dto.response.ChatResponse;
import com.palettee.chat.repository.ChatRepository;
import com.palettee.chat.service.ChatService;
import com.palettee.chat_room.domain.ChatCategory;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.chat_room.repository.ChatRoomRepository;
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
    @DisplayName("채팅 저장")
    void saveChat() {
        // given
        String userEmail = savedUser.getEmail();
        Long chatRoomId = savedChatRoom.getId();
        ChatRequest chatRequest = new ChatRequest("Hi");

        // when
        ChatResponse chatResponse = chatService.saveChat(userEmail, chatRoomId, chatRequest);

        // then
        assertThat(chatResponse.chatId()).isNotNull();
        assertThat(chatResponse.email()).isEqualTo(userEmail);
        assertThat(chatResponse.profileImg()).isEqualTo("imageUrl");
        assertThat(chatResponse.content()).isEqualTo("Hi");
    }

    @Test
    @DisplayName("채팅 이미지 저장")
    void saveChatImage() {
        // given
        String userEmail = savedUser.getEmail();
        Long chatRoomId = savedChatRoom.getId();
        List<ChatImgUrlRequest> urls = new ArrayList<>();
        ChatImgRequest chatImgRequest = new ChatImgRequest(urls);

        urls.add(new ChatImgUrlRequest("aaa"));
        urls.add(new ChatImgUrlRequest("bbb"));
        urls.add(new ChatImgUrlRequest("ccc"));

        // when
        ChatImgResponse chatImgResponse = chatService.saveImageMessage(userEmail, chatRoomId, chatImgRequest);

        // then
        assertThat(chatImgResponse.chatId()).isNotNull();
        assertThat(chatImgResponse.email()).isEqualTo(userEmail);
        assertThat(chatImgResponse.profileImg()).isEqualTo("imageUrl");
        assertThat(chatImgResponse.imgUrls().size()).isEqualTo(3);
    }
}
