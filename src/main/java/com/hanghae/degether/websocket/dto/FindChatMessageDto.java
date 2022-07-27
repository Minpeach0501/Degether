package com.hanghae.degether.websocket.dto;

import com.hanghae.degether.project.dto.UserDto;
import com.hanghae.degether.websocket.model.ChatMessage;

import java.time.LocalDateTime;

// 프로젝션
public interface FindChatMessageDto {
    Long getId();
    ChatMessage.MessageType getType();

    String getProjectId();

    String getMessage();
    String getCreatedAt();
    user getUser();

    interface user{
        Long getId();
        String getProfileUrl();
        String getRole();
        String getNickname();
    }
}