package com.hanghae.degether.WebSocket.dto;

import com.hanghae.degether.WebSocket.model.Message;

public interface FindMessageDto {

    Message.MessageType getType();
    String getMessage();
    String getProfileUrl();
    String getUsername();
}
