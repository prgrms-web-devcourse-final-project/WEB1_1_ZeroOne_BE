package com.palettee.chat.repository;

import com.palettee.chat.service.dto.ChatImageSaveDto;
import com.palettee.chat.service.dto.ChatSaveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchInsertChats(List<ChatSaveDto> chats, List<ChatImageSaveDto> chatImages) {
        String sql = "INSERT INTO chat"
                + "(chat_id, user_id, chat_room_id, content, send_at) VALUE(?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ChatSaveDto chat = chats.get(i);
                ps.setString(1, chat.chatId());
                ps.setLong(2, chat.userId());
                ps.setLong(3, chat.chatRoomId());
                ps.setString(4, chat.content());
                ps.setTimestamp(5, Timestamp.valueOf(chat.sendAt()));
            }

            @Override
            public int getBatchSize() {
                return chats.size();
            }
        });

        String chatImageSql = "INSERT INTO chat_image"
                + "(chat_id, image_url) VALUE(?, ?)";
        jdbcTemplate.batchUpdate(chatImageSql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ChatImageSaveDto chatImage = chatImages.get(i);
                ps.setString(1, chatImage.chatId());
                ps.setString(2, chatImage.imgUrl());
            }

            @Override
            public int getBatchSize() {
                return chatImages.size();
            }
        });
    }
}
