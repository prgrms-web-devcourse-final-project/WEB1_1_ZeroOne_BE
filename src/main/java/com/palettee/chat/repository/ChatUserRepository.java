package com.palettee.chat.repository;

import com.palettee.chat.domain.*;
import com.palettee.user.domain.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
    @Query("SELECT cu from ChatUser cu " +
            "WHERE cu.chatRoom.id = :chatRoomId " +
            "AND cu.user.id = :userId ")
    Optional<ChatUser> findByChatRoomAndUser(@Param("chatRoomId") Long chatRoomId,
                                             @Param("userId") Long userId);

    @Query("SELECT count(cu) from ChatUser cu WHERE cu.chatRoom.id = :chatRoomId And cu.isDeleted = :isDeleted")
    int countChatUsersByChatRoom(@Param("chatRoomId") Long chatRoomId,
                                 @Param("isDeleted") boolean isDeleted);

    @Modifying
    @Query("delete from ChatUser cu where cu.chatRoom.id = :chatRoomId")
    void deleteAllByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    @Query("select cu from ChatUser cu join fetch cu.user where cu.chatRoom in " +
            "(select cu2.chatRoom from ChatUser cu2 where cu2.user = :user)" +
            "and cu.user <> :user")
    List<ChatUser> getChatUsersByMe(@Param("user") User user);
}
