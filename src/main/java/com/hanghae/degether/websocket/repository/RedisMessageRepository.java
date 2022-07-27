package com.hanghae.degether.websocket.repository;

import com.hanghae.degether.websocket.dto.FindChatMessageDto;
import com.hanghae.degether.websocket.model.ChatMessage;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface RedisMessageRepository extends CrudRepository<ChatMessage, Long> {

    List<FindChatMessageDto> findAllMessageByProjectId(String roomId);

    List<ChatMessage> findTop100MessageByProjectId(String projectId);
}
