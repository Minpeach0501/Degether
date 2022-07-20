package com.hanghae.degether.project.service;

import com.hanghae.degether.project.dto.CommentDto;
import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
import com.hanghae.degether.project.model.Comment;
import com.hanghae.degether.project.repository.CommentRepository;
import com.hanghae.degether.project.repository.ProjectRepository;
import com.hanghae.degether.project.repository.UserProjectRepository;
import com.hanghae.degether.project.util.CommonUtil;
import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final ProjectRepository projectRepository;
    private final CommentRepository commentRepository;
    private final UserProjectRepository userProjectRepository;
    @Transactional
    public Long createComment(Long projectId, CommentDto.Request commentRequestDto) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        Comment savedComment = commentRepository.save(Comment.builder()
                .user(user)
                .comment(commentRequestDto.getComment())
                .build());
        project.getComments().add(savedComment);
        return savedComment.getId();
    }
    @Transactional
    public void deleteComment(Long commentId) {
        User user = CommonUtil.getUser();
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new CustomException(ErrorCode.NOT_EXIST_COMMENT));
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        commentRepository.delete(comment);
    }
}
