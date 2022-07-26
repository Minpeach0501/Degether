package com.hanghae.degether.websocket.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @Column
    private String roomId;


    //프로젝트 생성시 채팅방 생성
    public static ChatRoom create(String roomId ) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomId = roomId;
        return chatRoom;
    }
}