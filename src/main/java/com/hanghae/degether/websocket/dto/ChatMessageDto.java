package com.hanghae.degether.websocket.dto;

import com.hanghae.degether.project.dto.UserDto;
import com.hanghae.degether.websocket.model.ChatMessage;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long id;
    private ChatMessage.MessageType type;

    private String projectId; // 프로젝트 번호

    private String message; // 메시지
    private String createdAt;
    private SenderDto user;
}