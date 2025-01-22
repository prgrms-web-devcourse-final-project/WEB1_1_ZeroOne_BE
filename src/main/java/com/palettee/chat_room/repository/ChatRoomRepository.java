package com.palettee.chat_room.repository;

import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.user.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Modifying
    @Query("delete from ChatRoom cr where cr.id = :chatRoomId")
    void deleteByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    @Query("select count(cr) > 0 from ChatRoom cr " +
            "where cr in (" +
            "select cu1.chatRoom from ChatUser cu1 " +
            "where cu1.user = :findUser and cu1.isDeleted = false)" +
            "and cr in (" +
            "select cu2.chatRoom from ChatUser cu2 " +
            "where cu2.user = :targetUser)")
    boolean existsChatRoomWithTwoUsers(@Param("findUser") User findUser,
                                       @Param("targetUser") User targetUser);
}
