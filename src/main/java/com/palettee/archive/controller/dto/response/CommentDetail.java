package com.palettee.archive.controller.dto.response;

import com.palettee.archive.domain.Comment;
import com.palettee.user.domain.User;

public record CommentDetail(
        Long commentId,
        String content,
        String username,
        boolean isMine
) {
    public static CommentDetail toResponse(Comment comment, User user) {
        Long userId = user == null ? 0L : user.getId();
        return new CommentDetail(
                comment.getId(),
                comment.getContent(),
                comment.getUsername(),
                comment.getUserId().equals(userId)
        );
    }
}
