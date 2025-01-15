package com.palettee.chat.repository;

import com.palettee.chat.domain.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRepository extends JpaRepository<Chat, Long>, ChatCustomRepository {
    @Modifying
    @Query("delete from Chat c where c.chatRoom.id = :chatRoomId")
    void bulkDeleteChatsByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
