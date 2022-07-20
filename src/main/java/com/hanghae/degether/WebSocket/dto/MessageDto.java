package com.hanghae.degether.WebSocket.dto;

import com.hanghae.degether.WebSocket.model.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Message.MessageType type; // 메시지 타입은 Message 클래스에 있음
    private String roomId; // 방번호 = projectId 값
    private String message; // 메시지
    private String nickname; // nickname user에서 받아온 값
    private String profileUrl; // user 에서 정보 받아오기
    private String username; // 유저 아이디값  "social + id"
    private String createdAt; // 생성시간

}