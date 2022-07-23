package com.hanghae.degether.websocket.dto;

import com.hanghae.degether.websocket.model.ChatMessage;

// 프로젝션
public interface FindChatMessageDto {

    ChatMessage.MessageType getType();

    String getRoomId();

    String getSender();

    String getMessage();

    String getProfileUrl();

    Long getUserId();

    String getCreatedAt();
}