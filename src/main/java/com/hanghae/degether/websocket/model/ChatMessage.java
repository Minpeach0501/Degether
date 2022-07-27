package com.hanghae.degether.websocket.model;

import com.hanghae.degether.project.model.Timestamped;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.websocket.dto.ChatMessageDto;
import lombok.*;

import javax.persistence.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChatMessage extends Timestamped {

    // 메시지 타입 : 입장, 퇴장, 채팅
    public enum MessageType {
        ENTER, QUIT, TALK
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String projectId; // 프로젝트 아이디값이 들어간다다

    @Enumerated(EnumType.STRING)
    @Column
    private MessageType type; // 메시지 타입

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 메시지 보낸사람

    @Column
    private String message; // 메시지


    // 변수에 값 집어넣기 위한 생성자
    public ChatMessage(ChatMessageDto chatMessageDto, ChatRoom chatRoom) {
        this.type = chatMessageDto.getType();
        this.roomId = chatMessageDto.getRoomId();
        this.message = chatMessageDto.getMessage();
        this.sender = chatMessageDto.getSender();
        this.profileUrl = chatMessageDto.getProfileUrl();
        this.createdAt = chatMessageDto.getCreatedAt();
        this.userId = chatMessageDto.getUserId();
        this.chatRoom = chatRoom;
    }


}
