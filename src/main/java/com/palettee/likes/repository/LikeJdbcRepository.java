package com.palettee.likes.repository;

import com.palettee.likes.controller.dto.LikeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LikeJdbcRepository {

    private final JdbcTemplate jdbcTemplate;


    public void batchInsertLike(List<LikeDto> likeDtoList){

        String sql = "INSERT INTO likes (target_id, user_id, like_type) VALUES (?, ?, ?)";


        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {


            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                LikeDto likeDto = likeDtoList.get(i);

                ps.setLong(1, likeDto.targetId());
                ps.setLong(2, likeDto.userId());
                ps.setString(3, likeDto.likeType().name().toUpperCase());
            }

            @Override
            public int getBatchSize() {
                return likeDtoList.size();
            }
        });

    }
}
