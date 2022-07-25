package com.hanghae.degether.websocket.repository;

import com.hanghae.degether.websocket.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<ChatRoom, Long> {

    ChatRoom findByRoomId(String roomId);
    //삭제 메서드
//    void deleteByRoomId(String roomId);

}
