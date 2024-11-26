package com.palettee.global.s3.controller;

import com.palettee.global.s3.controller.dto.response.ImagesResponse;
import com.palettee.global.s3.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/upload")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/images")
    public ImagesResponse imagesUpload(@RequestPart(value = "files") List<MultipartFile> files) {
        return imageService.upload(files);
    }
}
