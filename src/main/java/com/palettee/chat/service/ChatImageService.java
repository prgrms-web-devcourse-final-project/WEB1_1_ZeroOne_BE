package com.palettee.chat.service;

import com.palettee.chat.controller.dto.response.ChatImgUploadResponse;
import com.palettee.chat.controller.dto.response.ChatImgUrlResponse;
import com.palettee.chat.domain.ChatImage;
import com.palettee.chat.repository.ChatImageRepository;
import com.palettee.global.s3.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatImageService {

    private final ChatImageRepository chatImageRepository;
    private final ImageService imageService;

    public List<ChatImage> saveChatImages(List<ChatImage> chatImages) {
        return chatImageRepository.saveAll(chatImages);
    }

    public ChatImgUploadResponse chatImgUpload(List<MultipartFile> files) {
        List<ChatImgUrlResponse> chatImgUrls =
                files.stream()
                        .map(file -> new ChatImgUrlResponse(imageService.uploadAndMakeImgUrl(file)))
                        .collect(Collectors.toList());

        return new ChatImgUploadResponse(chatImgUrls);
    }
}
