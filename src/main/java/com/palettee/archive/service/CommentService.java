package com.palettee.archive.service;

import com.palettee.archive.controller.dto.request.CommentWriteRequest;
import com.palettee.archive.controller.dto.response.CommentResponse;
import com.palettee.archive.domain.Archive;
import com.palettee.archive.domain.Comment;
import com.palettee.archive.exception.ArchiveNotFound;
import com.palettee.archive.exception.CanNotCommentArchive;
import com.palettee.archive.repository.ArchiveRepository;
import com.palettee.archive.repository.CommentRepository;
import com.palettee.user.domain.User;
import com.palettee.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArchiveRepository archiveRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse writeComment(String email, Long archiveId, CommentWriteRequest commentWriteRequest) {
        User user = getUser(email);
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
        commentRepository.deleteById(commentId);
        return new CommentResponse(commentId);
    }

    private Archive getArchive(Long archiveId) {
        return archiveRepository.findById(archiveId).orElseThrow(() -> ArchiveNotFound.EXCEPTION);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
    }
}
