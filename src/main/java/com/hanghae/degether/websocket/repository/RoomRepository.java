package com.hanghae.degether.websocket.repository;

import com.hanghae.degether.websocket.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<ChatRoom, Long> {
    ChatRoom findByUsername(String username);

    ChatRoom findByRoomId(String roomId);

    void deleteByRoomId(String roomId);
}
