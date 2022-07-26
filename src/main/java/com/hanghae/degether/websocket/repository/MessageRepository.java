package com.hanghae.degether.websocket.repository;

import com.hanghae.degether.websocket.dto.FindChatMessageDto;
import com.hanghae.degether.websocket.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<ChatMessage, Long> {
    List<FindChatMessageDto> findAllByRoomId(String roomId);

    ChatMessage findByRoomId(String roomId);

    // 최근 10개의 기록만 가져오기
    //아직 필요한 곳을 찾지 못한 기능들
//    ChatMessage findTop1ByRoomIdOrderByCreatedAtDesc(String roomId);
//    List<ChatMessage> findAllByChatRoom(ChatRoom chatRoom);
//    void deleteByChatRoom(ChatRoom chatRoom);
}
