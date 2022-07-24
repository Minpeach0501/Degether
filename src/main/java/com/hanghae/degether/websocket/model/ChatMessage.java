package com.hanghae.degether.websocket.model;

import com.hanghae.degether.websocket.dto.ChatMessageDto;
import lombok.*;

import javax.persistence.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChatMessage {

    // 메시지 타입 : 입장, 퇴장, 채팅
    public enum MessageType {
        ENTER, QUIT, TALK
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String roomId; // 프로젝트 아이디값이 들어간다다

    @Column
    private MessageType type; // 메시지 타입

    @Column
    private String sender; // 메시지 보낸사람 닉네임 값이 들어간다

    @Column
    private String message; // 메시지

    @Column
    private String profileUrl; // 프로필 사진

    @Column
    private String createdAt;  // 생성 날짜

    @Column
    private Long userId;  // DB에 저장된 USER의 ID 값

    @JoinColumn(name = "CHAT_ROOM_ID")
    @ManyToOne
    private ChatRoom chatRoom;


    // 변수에 값 집어넣기 위한 생성자
    public ChatMessage(ChatMessageDto chatMessageDto, ChatRoom chatRoom) {
        this.type = chatMessageDto.getType();
        this.roomId = chatMessageDto.getRoomId();
        this.message = chatMessageDto.getMessage();
        this.sender = chatMessageDto.getSender();
        this.profileUrl = chatMessageDto.getProfileUrl();
        this.chatRoom = chatRoom;
    }


}
