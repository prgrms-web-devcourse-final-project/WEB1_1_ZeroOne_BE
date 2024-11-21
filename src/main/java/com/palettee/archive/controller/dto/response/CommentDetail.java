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
        Long userId = 0L;
        if(user != null) {
            userId = user.getId();
        }
        return new CommentDetail(
                comment.getId(),
                comment.getContent(),
                comment.getUsername(),
                comment.getUserId().equals(userId)
        );
    }
}
