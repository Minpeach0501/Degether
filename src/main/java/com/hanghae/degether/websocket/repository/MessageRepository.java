package com.hanghae.degether.websocket.repository;

import com.hanghae.degether.websocket.dto.FindChatMessageDto;
import com.hanghae.degether.websocket.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<ChatMessage, Long> {
    List<FindChatMessageDto> findTop10ByProjectIdOrderByIdDesc(String projectId);


}
