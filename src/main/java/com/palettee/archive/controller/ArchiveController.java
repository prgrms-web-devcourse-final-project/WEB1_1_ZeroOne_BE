package com.palettee.archive.controller;

import com.palettee.archive.controller.dto.request.ArchiveRegisterRequest;
import com.palettee.archive.controller.dto.request.ArchiveUpdateRequest;
import com.palettee.archive.controller.dto.response.ArchiveDetailResponse;
import com.palettee.archive.controller.dto.response.ArchiveResponse;
import com.palettee.archive.service.ArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/archive")
public class ArchiveController {

    private final ArchiveService archiveService;

    @PostMapping
    public ArchiveResponse registerArchive(@RequestBody ArchiveRegisterRequest archiveRegisterRequest) {
        return archiveService.registerArchive(archiveRegisterRequest, getUserName());
    }

    @GetMapping("/{archiveId}")
    public ArchiveDetailResponse getArchive(@PathVariable("archiveId") long archiveId) {
        return archiveService.getArchiveDetail(archiveId);
    }

    @PutMapping("/{archiveId}")
    public ArchiveResponse updateArchive(@PathVariable("archiveId") long archiveId, @RequestBody ArchiveUpdateRequest archiveUpdateRequest) {
        return archiveService.updateArchive(archiveId, archiveUpdateRequest);
    }

    @DeleteMapping("/{archiveId}")
    public ArchiveResponse deleteArchive(@PathVariable("archiveId") long archiveId) {
        return archiveService.deleteArchive(archiveId);
    }

    private String getUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
