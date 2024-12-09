package com.palettee.chat.repository;

import com.palettee.chat.domain.*;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.user.domain.User;
import org.springframework.data.jpa.repository.*;

import java.util.Optional;

public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
    Optional<ChatUser> findByChatRoomAndUser(ChatRoom chatRoom, User user);
    boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);
}
