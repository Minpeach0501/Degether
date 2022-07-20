package com.hanghae.degether.WebSocket.dto;

import com.hanghae.degether.WebSocket.model.Message;

public interface FindMessageDto {

    Message.MessageType getType();
    String getRoomId();
    String getSender();
    String getMessage();
    String getProfileUrl();
    Long getEnterUserCnt();
    String getUsername();
    String getCreatedAt();
    String getFileUrl();
}
