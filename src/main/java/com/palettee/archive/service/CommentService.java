package com.palettee.archive.service;

import com.palettee.archive.controller.dto.request.CommentWriteRequest;
import com.palettee.archive.controller.dto.response.CommentDetail;
import com.palettee.archive.controller.dto.response.CommentListResponse;
import com.palettee.archive.controller.dto.response.CommentResponse;
import com.palettee.archive.controller.dto.response.SliceInfo;
import com.palettee.archive.domain.Archive;
import com.palettee.archive.domain.Comment;
import com.palettee.archive.exception.ArchiveNotFound;
import com.palettee.archive.exception.CanNotCommentArchive;
import com.palettee.archive.exception.CommentNotFound;
import com.palettee.archive.repository.ArchiveRepository;
import com.palettee.archive.repository.CommentRepository;
import com.palettee.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArchiveRepository archiveRepository;

    @Transactional
    public CommentResponse writeComment(User user, Long archiveId, CommentWriteRequest commentWriteRequest) {
        Archive archive = getArchive(archiveId);
        checkCommentOpen(archive);
        Comment savedComment = commentRepository.save(new Comment(commentWriteRequest.content(), user.getName(), user.getId(), archive));
        return new CommentResponse(savedComment.getId());
    }

    private void checkCommentOpen(Archive archive) {
        if (archive.isNotOpenComment()) {
            throw CanNotCommentArchive.EXCEPTION;
        }
    }

    @Transactional
    public CommentResponse deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> CommentNotFound.EXCEPTION);
        commentRepository.delete(comment);
        return new CommentResponse(commentId);
    }

    public CommentListResponse getComment(User user, Long archiveId, Pageable pageable) {
        Archive archive = getArchive(archiveId);
        Slice<Comment> comments = commentRepository.findCommentWithArchiveId(archive.getId(), pageable);

        List<CommentDetail> result = comments.getContent().stream()
                .map(it -> CommentDetail.toResponse(it, user))
                .toList();
        return new CommentListResponse(result, SliceInfo.of(comments));
    }

    private Archive getArchive(Long archiveId) {
        return archiveRepository.findById(archiveId).orElseThrow(() -> ArchiveNotFound.EXCEPTION);
    }
}
