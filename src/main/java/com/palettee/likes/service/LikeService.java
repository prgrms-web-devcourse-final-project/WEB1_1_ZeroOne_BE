package com.palettee.likes.service;

import com.palettee.likes.controller.dto.LikeDto;
import com.palettee.likes.repository.LikeJdbcRepository;
import com.palettee.likes.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService {

    private final LikeJdbcRepository likeRepository;

    public void bulkSaveLike(List<LikeDto> list){
        likeRepository.batchInsertLike(list);
    }
}
