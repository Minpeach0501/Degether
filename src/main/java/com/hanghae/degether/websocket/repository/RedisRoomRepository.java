package com.hanghae.degether.websocket.repository;

import com.hanghae.degether.websocket.model.ChatRoom;
import org.springframework.data.repository.CrudRepository;

// redis 저장용 repository >> 주로 save를 한다 .
public interface RedisRoomRepository extends CrudRepository<ChatRoom, Long> {


    // 삭제시
    // void  deleteByRoomId(roomId);
}
