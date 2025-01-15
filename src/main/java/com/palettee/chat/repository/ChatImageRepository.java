package com.palettee.chat.repository;

import com.palettee.chat.domain.*;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.*;

public interface ChatImageRepository extends JpaRepository<ChatImage, Long> {
    @Modifying
    @Query("DELETE FROM ChatImage ci WHERE ci.chat IN (SELECT c FROM Chat c WHERE c.chatRoom.id = :chatRoomId)")
    void bulkDeleteChatImagesByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}

