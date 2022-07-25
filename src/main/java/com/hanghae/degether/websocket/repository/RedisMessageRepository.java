package com.hanghae.degether.websocket.repository;

import com.hanghae.degether.websocket.model.ChatMessage;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RedisMessageRepository extends CrudRepository<ChatMessage, Long> {

    List<ChatMessage> findAllMessageByRoomId(String roomId);


}
