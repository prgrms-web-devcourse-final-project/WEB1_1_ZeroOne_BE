package com.palettee.archive;

import static org.assertj.core.api.Assertions.*;

import com.palettee.archive.controller.dto.request.*;
import com.palettee.archive.controller.dto.response.*;
import com.palettee.archive.domain.*;
import com.palettee.archive.exception.*;
import com.palettee.archive.repository.*;
import com.palettee.archive.service.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.*;

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
        savedUser = userRepository.save(
                User.builder()
                        .email("email")
                        .imageUrl("imageUrl")
                        .name("name")
                        .briefIntro("briefIntro")
                        .userRole(UserRole.USER)
                        .majorJobGroup(MajorJobGroup.DEVELOPER)
                        .minorJobGroup(MinorJobGroup.BACKEND)
                        .build()
        );
        savedArchiveCanComment = archiveRepository.save(new Archive("title", "description", "introduction", ArchiveType.RED, true, savedUser));
        savedArchiveNotComment = archiveRepository.save(new Archive("title", "description", "introduction", ArchiveType.RED, false, savedUser));
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
        archiveRepository.deleteAll();
        commentRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적인 댓글 작성 성공")
    void writeCommentTest() {
        // given
        CommentWriteRequest request = new CommentWriteRequest("content");

        // when
        CommentResponse response = commentService.writeComment(savedUser, savedArchiveCanComment.getId(), request);

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
        assertThatThrownBy(() -> commentService.writeComment(savedUser, savedArchiveNotComment.getId(), request))
                .isInstanceOf(CanNotCommentArchive.class);

    }

    @Test
    @DisplayName("정상적인 댓글 삭제 성공")
    void deleteCommentTest() {
        // given
        CommentWriteRequest request = new CommentWriteRequest("content");

        // when
        CommentResponse response = commentService.writeComment(savedUser, savedArchiveCanComment.getId(), request);
        CommentResponse commentResponse = commentService.deleteComment(response.commentId(), savedUser.getId());

        // then
        Optional<Comment> comment = commentRepository.findById(commentResponse.commentId());
        assertThat(comment.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("정상적인 댓글 전체 조회 성공")
    void getCommentsTestWithArchive() {
        // given
        CommentWriteRequest request = new CommentWriteRequest("content");
        for (int i = 0; i < 5; i++) {
            commentService.writeComment(savedUser, savedArchiveCanComment.getId(), request);
        }

        // when
        CommentListResponse comment = commentService.getCommentWithArchive(savedUser, savedArchiveCanComment.getId(),
                PageRequest.of(0, 10));

        // then
        assertThat(comment.comments().size()).isEqualTo(5);
    }

}
