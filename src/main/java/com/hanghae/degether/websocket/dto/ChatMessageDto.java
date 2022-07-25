package com.hanghae.degether.websocket.dto;

import com.hanghae.degether.websocket.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

    private ChatMessage.MessageType type;

    private String roomId; // 방번호

    private String message; // 메시지

    private String sender; // nickname

    private String profileUrl; // 포로필 사진

    private Long userId;  //DB에 저장된 유저 아이디값

    private String createdAt; // 생성시간



    public ChatMessageDto(FindChatMessageDto chatMessage) {
        this.type = chatMessage.getType();
        this.roomId = chatMessage.getRoomId();
        this.message =chatMessage.getMessage();
        this.sender = chatMessage.getSender();
        this.profileUrl = chatMessage.getProfileUrl();
        this.userId = chatMessage.getUserId();
        this.createdAt = chatMessage.getCreatedAt();
    }

    public ChatMessageDto(ChatMessage chatMessage) {
        this.type = chatMessage.getType();
        this.roomId = chatMessage.getRoomId();
        this.message =chatMessage.getMessage();
        this.sender = chatMessage.getSender();
        this.profileUrl = chatMessage.getProfileUrl();
        this.userId = chatMessage.getUserId();
        this.createdAt = chatMessage.getCreatedAt();
    }

}