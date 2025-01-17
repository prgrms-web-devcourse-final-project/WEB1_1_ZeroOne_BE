package com.palettee.image;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ImageProcessor {

    Pattern pattern = Pattern.compile("!\\[.*?]\\((https?://[^)]+)\\)");

    /**
     *  본문에서 이미지 태그를 추출해서 이미지 링크를 전부 가져옵니다.
     * @param content
     * @return List
     */
    public List<String> parseImageTag(String content) {
        Matcher matcher = pattern.matcher(content);

        List<String> imageUrls = new ArrayList<>();
        while (matcher.find()) {
            imageUrls.add(matcher.group(1));
        }
        return imageUrls;
    }
}
