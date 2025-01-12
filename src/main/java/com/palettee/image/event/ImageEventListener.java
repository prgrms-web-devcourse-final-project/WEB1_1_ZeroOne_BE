package com.palettee.image.event;

import com.palettee.archive.domain.ArchiveImage;
import com.palettee.archive.repository.ArchiveImageRepository;
import com.palettee.image.ImageProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ImageEventListener {

    private final ArchiveImageRepository archiveImageRepository;
    private final ImageProcessor imageProcessor;

    @Transactional
    @EventListener(ImageProcessingEvent.class)
    public void process(ImageProcessingEvent event) {
        List<String> imageUrls = imageProcessor.parseImageTag(event.content());
        Long archiveId = event.targetId();
        for (String imageUrl : imageUrls) {
            archiveImageRepository.save(new ArchiveImage(imageUrl, archiveId));
        }
    }

}
