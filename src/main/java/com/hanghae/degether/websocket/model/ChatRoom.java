package com.hanghae.degether.websocket.model;

import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.websocket.dto.UserInfoDto;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    @Column
    private String roomId;

    private String username;

    //프로젝트 생성시 채팅방 생성
    public static ChatRoom create(Project project, UserInfoDto userDto) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomId = String.valueOf(project.getId());
        chatRoom.username = userDto.getUsername();
        return chatRoom;
    }

    public static ChatRoom create(String roomId) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomId = roomId;
        return chatRoom;
    }
}