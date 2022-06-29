package com.hanghae.degether.project.controller;

import com.hanghae.degether.project.dto.CommentDto;
import com.hanghae.degether.project.dto.ResponseDto;
import com.hanghae.degether.project.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/comment/{projectId}")
    public ResponseDto<?> createComment(@PathVariable Long projectId, @RequestBody CommentDto.Request commentRequestDto) {
        return ResponseDto.builder()
                .ok(true)
                .message("작성 성공")
                .result(commentService.createComment(projectId, commentRequestDto))
                .build();
    }
    @DeleteMapping("/comment/{commentId}")
    public ResponseDto<?> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseDto.builder()
                .ok(true)
                .message("삭제 성공")
                .build();
    }
}
