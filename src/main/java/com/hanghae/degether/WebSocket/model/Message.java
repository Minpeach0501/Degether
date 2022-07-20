package com.hanghae.degether.WebSocket.model;

import com.hanghae.degether.WebSocket.dto.MessageDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Message {

    public enum MessageType{
       // 입장시 , 말할시 , 나갈시
        ENTER,TALK,QUIT
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private MessageType type;
    @Column
    //방 아이디값
    private String roomId;
    @Column
    // 보내는사람
    private String nickname;
    @Column
    //메세지
    private String message;

    @Column
    private String profileUrl;

    @Column
    private String username;
    @Column
    private String createdAt;

    @JoinColumn(name = "CHAT_ROOM_ID")
    @ManyToOne
    private ChatRoom chatRoom;

    public Message(MessageDto MessageDto, ChatRoom chatRoom) {
        this.type = MessageDto.getType();
        this.roomId = MessageDto.getRoomId();
        this.message = MessageDto.getMessage();
        this.nickname = MessageDto.getNickname();
        this.profileUrl = MessageDto.getProfileUrl();
        this.username = MessageDto.getUsername();
        this.createdAt = MessageDto.getCreatedAt();
        this.chatRoom = chatRoom;
    }

}
