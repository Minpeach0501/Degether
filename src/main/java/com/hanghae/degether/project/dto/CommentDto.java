package com.hanghae.degether.project.dto;

import lombok.*;


public class CommentDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request{
        private String comment;
    }
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        private Long commentId;
        private String nickname;
        private String comment;
    }

}
