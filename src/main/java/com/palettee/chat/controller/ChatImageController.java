package com.palettee.chat.controller;

import com.palettee.chat.controller.dto.response.ChatImgUploadResponse;
import com.palettee.chat.service.ChatImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/chat-image")
@RequiredArgsConstructor
public class ChatImageController {

    private final ChatImageService chatImageService;

    @PostMapping("/upload")
    public ChatImgUploadResponse chatImagesUpload(@RequestPart(value = "files") List<MultipartFile> files) {
        return chatImageService.chatImgUpload(files);
    }
}
