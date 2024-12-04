package com.palettee.chat.repository;

import com.palettee.chat.domain.Chat;
import com.palettee.chat.domain.ChatImage;
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

    public void batchInsertChats(List<Chat> chats, List<ChatImage> chatImages) {
        String sql = "INSERT INTO chat"
                + "(chat_id, user_id, chat_room_id, content, send_at) VALUE(?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Chat chat = chats.get(i);
                ps.setString(1, chat.getId());
                ps.setLong(2, chat.getUser().getId());
                ps.setLong(3, chat.getChatRoom().getId());
                ps.setString(4, chat.getContent());
                ps.setTimestamp(5, Timestamp.valueOf(chat.getSendAt()));
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
                ChatImage chatImage = chatImages.get(i);
                ps.setString(1, chatImage.getChat().getId());
                ps.setString(2, chatImage.getImageUrl());
            }

            @Override
            public int getBatchSize() {
                return chatImages.size();
            }
        });
    }
}
