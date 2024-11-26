package com.palettee.global.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.palettee.global.s3.exception.BadFileExtensionException;
import com.palettee.global.s3.exception.FileEmptyException;
import com.palettee.global.s3.service.ImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class ImageServiceTest {

    @Value("${aws.s3.bucket}")
    String bucket;

    @Value("${aws.s3.base-url}")
    String baseUrl;

    @Autowired
    AmazonS3 s3Client;

    @Autowired
    ImageService imageService;

    @Test
    @DisplayName("존재하지 않는 파일 예외")
    void throwFileNoExistException() {
        // given
        MockMultipartFile emptyMultipartFile = new MockMultipartFile(
                "비어있는 이미지",
                "emptyFile.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[0]
        );

        // when & then
        assertThatThrownBy(() -> imageService.isExistFile(emptyMultipartFile))
                .isInstanceOf(FileEmptyException.class);
    }

    @Test
    @DisplayName("지원하지 않는 파일 확장자 예외")
    void throwFileExtensionException() {
        // given
        String fileName = "image.pdf";

        // when & then
        assertThatThrownBy(() -> imageService.separateExt(fileName))
                .isInstanceOf(BadFileExtensionException.class);
    }

    @Test
    @DisplayName("파일 확장자 분리 성공")
    void separateFileExtension() {
        // given
        String fileName = "image.png";

        // when
        String ext = imageService.separateExt(fileName);

        //then
        assertThat(ext).isEqualTo("png");
    }

    @Test
    @DisplayName("파일 S3 업로드 성공")
    void uploadS3() {
        // given
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "testFile.png",
                MediaType.IMAGE_PNG_VALUE,
                "testFile".getBytes()
        );

        String fileName = UUID.randomUUID().toString() + ".png";

        // when
        imageService.uploadS3(multipartFile, fileName);
        boolean isFileUpload = s3Client.doesObjectExist(bucket, fileName);

        // then
        assert(isFileUpload);
    }

    @Test
    @DisplayName("imgUrl 추출 메서드")
    void uploadAndMakeImgUrl() {
        // given
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "testFile.png",
                MediaType.IMAGE_PNG_VALUE,
                "testFile".getBytes()
        );

        // when
        String imgUrl = imageService.uploadAndMakeImgUrl(multipartFile);

        // then
        assertThat(imgUrl).startsWith(baseUrl);
        assertThat(isUrlAccessible(imgUrl)).isTrue();
    }

    @Test
    @DisplayName("S3 Object 삭제")
    void deleteObject() {
        // given
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "testFile.png",
                MediaType.IMAGE_PNG_VALUE,
                "testFile".getBytes()
        );

        String imgUrl = imageService.uploadAndMakeImgUrl(multipartFile);

        // when
        String bucketKey = imageService.getBucketKey(imgUrl);
        imageService.delete(imgUrl);

        //then
        assertThat(s3Client.doesObjectExist(bucket, bucketKey)).isFalse();
    }

    private boolean isUrlAccessible(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();

            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            return false;
        }
    }
}
