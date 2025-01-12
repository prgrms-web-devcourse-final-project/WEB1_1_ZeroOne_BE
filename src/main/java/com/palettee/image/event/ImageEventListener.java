package com.palettee.image.event;

import com.palettee.archive.domain.ArchiveImage;
import com.palettee.archive.repository.ArchiveImageRepository;
import com.palettee.global.s3.service.ImageService;
import com.palettee.image.ImageProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ImageEventListener {

    private final ArchiveImageRepository archiveImageRepository;
    private final ImageProcessor imageProcessor;
    private final ImageService imageService;

    @Async
    @Transactional
    @EventListener(ImageProcessingEvent.class)
    public void process(ImageProcessingEvent event) {
        List<String> imageUrls = imageProcessor.parseImageTag(event.content());
        Long archiveId = event.targetId();

        deleteImages(archiveId);

        for (String imageUrl : imageUrls) {
            archiveImageRepository.save(new ArchiveImage(imageUrl, archiveId));
        }
    }

    private void deleteImages(Long archivedId) {
        List<String> OldImageUrls = archiveImageRepository.findAllByArchiveId(archivedId);

        for (String oldImageUrl : OldImageUrls) {
            imageService.delete(oldImageUrl);
        }

        archiveImageRepository.deleteAllByArchiveId(archivedId);
    }

}
