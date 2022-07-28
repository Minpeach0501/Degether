package com.hanghae.degether.sse;

import com.hanghae.degether.project.dto.UserDto;
import com.hanghae.degether.user.model.User;
import lombok.*;

import javax.persistence.Entity;

public class NotificationDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{

        private Long id;
        private String content;
        private Boolean isRead;
    }
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Publish{

        private Long id;
        private String content;
        private Boolean isRead;
        private Long reciverId;
    }
}
