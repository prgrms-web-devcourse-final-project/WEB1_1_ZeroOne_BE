package com.palettee.image.event;

import com.palettee.image.ImageProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageEventListener {

    private final ImageProcessor imageProcessor;

    @EventListener(ImageProcessingEvent.class)
    public void process(ImageProcessingEvent event) {
        List<String> imageUrls = imageProcessor.parseImageTag(event.content());

    }

}
