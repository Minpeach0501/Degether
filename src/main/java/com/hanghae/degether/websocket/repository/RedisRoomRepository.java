package com.hanghae.degether.websocket.repository;

import com.hanghae.degether.websocket.model.ChatRoom;
import org.springframework.data.repository.CrudRepository;

public interface RedisRoomRepository extends CrudRepository<ChatRoom, Long> {

}
