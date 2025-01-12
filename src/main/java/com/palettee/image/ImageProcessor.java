package com.palettee.image;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ImageProcessor {

    Pattern pattern = Pattern.compile("!\\[.*?]\\((https?://[^)]+)\\)");

    public List<String> parseImageTag(String content) {
        Matcher matcher = pattern.matcher(content);

        // URL 저장 리스트
        List<String> imageUrls = new ArrayList<>();
        while (matcher.find()) {
            // 캡처 그룹에서 URL 추출
            imageUrls.add(matcher.group(1));
        }
        return imageUrls;
    }
}
