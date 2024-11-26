package com.palettee.chat.service;

import com.palettee.chat.domain.ChatImage;
import com.palettee.chat.repository.ChatImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatImageService {

    private final ChatImageRepository chatImageRepository;

    public List<ChatImage> saveChatImages(List<ChatImage> chatImages) {
        return chatImageRepository.saveAll(chatImages);
    }
}
