package com.palettee.archive.controller;

import com.palettee.archive.controller.dto.request.ArchiveRegisterRequest;
import com.palettee.archive.controller.dto.request.ArchiveUpdateRequest;
import com.palettee.archive.controller.dto.request.ChangeOrderRequest;
import com.palettee.archive.controller.dto.request.CommentWriteRequest;
import com.palettee.archive.controller.dto.response.ArchiveDetailResponse;
import com.palettee.archive.controller.dto.response.ArchiveListResponse;
import com.palettee.archive.controller.dto.response.ArchiveResponse;
import com.palettee.archive.controller.dto.response.CommentListResponse;
import com.palettee.archive.controller.dto.response.CommentResponse;
import com.palettee.archive.service.ArchiveService;
import com.palettee.archive.service.CommentService;
import com.palettee.global.security.validation.UserUtils;
import com.palettee.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/archive")
public class ArchiveController {

    private final ArchiveService archiveService;
    private final CommentService commentService;

    @PostMapping
    public ArchiveResponse registerArchive(@Valid @RequestBody ArchiveRegisterRequest archiveRegisterRequest) {
        User user = UserUtils.getContextUser();
        return archiveService.registerArchive(archiveRegisterRequest, user);
    }

    @GetMapping("/{archiveId}")
    public ArchiveDetailResponse getArchive(@PathVariable("archiveId") long archiveId) {
        return archiveService.getArchiveDetail(archiveId);
    }

    @GetMapping
    public ArchiveListResponse getArchives(
            @RequestParam String majorJobGroup,
            @RequestParam String minorJobGroup,
            @RequestParam String sort,
            Pageable pageable
    ) {
        return archiveService.getAllArchive(majorJobGroup, minorJobGroup, sort, pageable);
    }

    @GetMapping("/search")
    public ArchiveListResponse searchArchives(
            @RequestParam String searchKeyword,
            Pageable pageable
    ) {
        return archiveService.searchArchive(searchKeyword, pageable);
    }

    @GetMapping("/me")
    public ArchiveListResponse getMyArchives() {
        User user = UserUtils.getContextUser();
        return archiveService.getMyArchive(user);
    }

    @GetMapping("/me/like")
    public ArchiveListResponse getMyLikeArchives() {
        User user = UserUtils.getContextUser();
        return archiveService.getLikeArchive(user);
    }

    @PutMapping("/{archiveId}")
    public ArchiveResponse updateArchive(@PathVariable("archiveId") long archiveId, @Valid @RequestBody ArchiveUpdateRequest archiveUpdateRequest) {
        return archiveService.updateArchive(archiveId, archiveUpdateRequest);
    }

    @DeleteMapping("/{archiveId}")
    public ArchiveResponse deleteArchive(@PathVariable("archiveId") long archiveId) {
        return archiveService.deleteArchive(archiveId);
    }

    @PatchMapping
    public void updateOrder(@Valid @RequestBody ChangeOrderRequest changeOrderRequest) {
        archiveService.changeArchiveOrder(changeOrderRequest);
    }

    @PostMapping("/{archiveId}")
    public ArchiveResponse likeArchive(@PathVariable("archiveId") Long archiveId) {
        return archiveService.likeArchive(archiveId, UserUtils.getContextUser());
    }

    @PostMapping("/{archiveId}/comment")
    public CommentResponse writeComment(@PathVariable("archiveId") long archiveId, @Valid @RequestBody CommentWriteRequest commentWriteRequest) {
        User user = UserUtils.getContextUser();
        return commentService.writeComment(user, archiveId, commentWriteRequest);
    }

    @GetMapping("/{archiveId}/comment")
    public CommentListResponse getComments(
            @PathVariable("archiveId") long archiveId,
            @RequestParam int page,
            @RequestParam int size
    ) {
        User user = UserUtils.getContextUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        return commentService.getComment(user, archiveId, pageRequest);
    }

    @DeleteMapping("/comment/{commentId}")
    public CommentResponse deleteComment(@PathVariable("commentId") long commentId) {
        User user = UserUtils.getContextUser();
        return commentService.deleteComment(commentId, user.getId());
    }

}
