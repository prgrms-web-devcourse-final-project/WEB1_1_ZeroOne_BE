package com.palettee.chat_room.repository;

import com.palettee.chat_room.domain.ChatRoom;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Modifying
    @Query("delete from ChatRoom cr where cr.id = :chatRoomId")
    void deleteByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
