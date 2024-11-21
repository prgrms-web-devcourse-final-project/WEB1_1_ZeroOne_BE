package com.palettee.archive;

import static org.assertj.core.api.Assertions.*;

import com.palettee.archive.controller.dto.request.CommentWriteRequest;
import com.palettee.archive.controller.dto.response.CommentResponse;
import com.palettee.archive.domain.Archive;
import com.palettee.archive.domain.ArchiveType;
import com.palettee.archive.domain.Comment;
import com.palettee.archive.exception.CanNotCommentArchive;
import com.palettee.archive.repository.ArchiveRepository;
import com.palettee.archive.repository.CommentRepository;
import com.palettee.archive.service.CommentService;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import com.palettee.user.domain.User;
import com.palettee.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class CommentServiceTest {

    @Autowired
    CommentService commentService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    ArchiveRepository archiveRepository;

    private User savedUser;
    private Archive savedArchiveCanComment;
    private Archive savedArchiveNotComment;

    @BeforeEach
    void beforeEach() {
        savedUser = userRepository.save(new User("email", "imageUrl","name", "briefIntro", MajorJobGroup.DEVELOPER, MinorJobGroup.BACKEND));
        savedArchiveCanComment = archiveRepository.save(new Archive("title", "description", ArchiveType.RED, true, savedUser));
        savedArchiveNotComment = archiveRepository.save(new Archive("title", "description", ArchiveType.RED, false, savedUser));
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
        commentRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적인 댓글 작성 성공")
    void writeCommentTest() {
        // given
        CommentWriteRequest request = new CommentWriteRequest("content");

        // when
        CommentResponse response = commentService.writeComment("email", savedArchiveCanComment.getId(), request);

        // then
        Comment comment = commentRepository.findById(response.commentId()).orElseThrow();
        assertThat(comment.getContent()).isEqualTo(request.content());
    }

    @Test
    @DisplayName("댓글이 허용되지 않은 게시글에 댓글 요청시 예외 처리")
    void writeCommentExceptionTest() {
        // given
        CommentWriteRequest request = new CommentWriteRequest("content");

        // when & then
        assertThatThrownBy(() -> commentService.writeComment("email", savedArchiveNotComment.getId(), request))
                .isInstanceOf(CanNotCommentArchive.class);

    }

}
