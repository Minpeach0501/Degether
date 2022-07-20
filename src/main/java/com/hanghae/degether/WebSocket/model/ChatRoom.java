package com.hanghae.degether.WebSocket.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ChatRoom {


    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @Column
    private String roomId;

    // 프로젝트 아이디 값으로 채팅방 개설
    public static ChatRoom create(Long projectId) {
        ChatRoom room = new ChatRoom();
        // 나중에 roomId 값은 프로젝트 아이디값을 받아와야한다.
        room.roomId = String.valueOf(projectId);

        return room;
    }
}