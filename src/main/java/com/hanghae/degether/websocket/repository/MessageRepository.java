package com.hanghae.degether.websocket.repository;

import com.hanghae.degether.websocket.dto.FindChatMessageDto;
import com.hanghae.degether.websocket.model.ChatMessage;
import com.hanghae.degether.websocket.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<ChatMessage, Long> {
    List<FindChatMessageDto> findAllByRoomId(String roomId);

    // 최근 10개의 기록만 가져오기
    ChatMessage findTop1ByRoomIdOrderByCreatedAtDesc(String roomId);

    List<ChatMessage> findAllByChatRoom(ChatRoom chatRoom);

    void deleteByChatRoom(ChatRoom chatRoom);
}
