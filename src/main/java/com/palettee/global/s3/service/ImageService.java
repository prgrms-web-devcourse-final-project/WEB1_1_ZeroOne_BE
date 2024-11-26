package com.palettee.global.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.palettee.global.s3.controller.dto.response.ImageUrlResponse;
import com.palettee.global.s3.controller.dto.response.ImagesResponse;
import com.palettee.global.s3.domain.FileExtension;
import com.palettee.global.s3.exception.BadFileExtensionException;
import com.palettee.global.s3.exception.FileEmptyException;
import com.palettee.global.s3.exception.FileUploadFailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.base-url}")
    private String baseUrl;

    public ImagesResponse upload(List<MultipartFile> files) {
        List<ImageUrlResponse> imageUrls = files.stream()
                .map(file -> new ImageUrlResponse(uploadAndMakeImgUrl(file)))
                .toList();

        return new ImagesResponse(imageUrls);
    }

    public String uploadAndMakeImgUrl(MultipartFile file) {
        isExistFile(file);

        String originalFilename = file.getOriginalFilename();
        String ext = separateExt(originalFilename);

        String randomName = UUID.randomUUID().toString();
        String fileName = randomName + "." + ext;

        uploadS3(file, fileName);

        return baseUrl + fileName;
    }

    public void isExistFile(MultipartFile file) {
        if (file.isEmpty() && file.getOriginalFilename() != null){
            throw FileEmptyException.EXCEPTION;
        }
    }

    public String separateExt(String originalFileName) {
        String ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        if (!FileExtension.isValidExtension(ext)) {
            throw BadFileExtensionException.EXCEPTION;
        }
        return ext;
    }

    public void uploadS3(MultipartFile file, String fileName) {
        try {
            ObjectMetadata objMeta = new ObjectMetadata();
            byte[] bytes = IOUtils.toByteArray(file.getInputStream()); // 파일의 데이터를 바이트로 읽습니다.
            objMeta.setContentType(file.getContentType()); // 파일의 타입을 지정합니다. 예) text, image
            objMeta.setContentLength(bytes.length); // 파일의 크기

            // PutObjectRequest를 사용하여 지정된 S3 버킷에 파일을 업로드
            s3Client.putObject(
                    new PutObjectRequest(bucket, fileName, file.getInputStream(), objMeta)
                            .withCannedAcl(CannedAccessControlList.PublicRead)); // ACL을 PublicRead로 하여 공용으로 읽을 수 있도록 권한 부여
        } catch (IOException e) {
            throw FileUploadFailException.EXCEPTION;
        }
    }

    public void delete(String profilePath) {
        String objectName = getBucketKey(profilePath);
        s3Client.deleteObject(bucket, objectName);
    }

    public String getBucketKey(String profilePath){
        return profilePath.substring(profilePath.lastIndexOf('/') + 1);
    }
}
