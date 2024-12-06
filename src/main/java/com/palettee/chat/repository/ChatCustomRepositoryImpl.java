package com.palettee.chat.repository;

import com.palettee.chat.controller.dto.response.ChatCustomResponse;
import com.palettee.chat.domain.Chat;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.palettee.chat.domain.QChat.*;
import static com.palettee.chat.domain.QChatImage.*;
import static com.palettee.chat_room.domain.QChatRoom.*;
import static com.palettee.user.domain.QUser.*;

@Repository
public class ChatCustomRepositoryImpl implements ChatCustomRepository{
    private final JPAQueryFactory queryFactory;

    public ChatCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public ChatCustomResponse findChatNoOffset(Long chatRoomId, int size, LocalDateTime sendAt) {
        List<Chat> chats = queryFactory
                .selectFrom(chat)
                .where(
                        chat.chatRoom.id.eq(chatRoomId),
                        sendAt != null ?
                                chat.sendAt.lt(sendAt) : null
                )
                .leftJoin(chat.chatImages, chatImage).fetchJoin()
                .leftJoin(chat.user, user).fetchJoin()
                .leftJoin(chat.chatRoom, chatRoom).fetchJoin()
                .orderBy(chat.sendAt.desc())
                .limit(size + 1)
                .fetch();

        boolean hasNext = chats.size() > size;

        LocalDateTime lastSendAt = null;
        if(hasNext) {
            if(size != 0) {
                lastSendAt = chats.get(size-1).getSendAt();
            }
            chats = chats.subList(0, size);
        }

        return ChatCustomResponse.toResponseFromEntity(chats, hasNext, lastSendAt);
    }
}
