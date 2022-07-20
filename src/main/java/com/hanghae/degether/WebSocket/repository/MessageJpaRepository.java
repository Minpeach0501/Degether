//package com.hanghae.degether.WebSocket.repository;
//
//import com.hanghae.degether.WebSocket.dto.FindMessageDto;
//import com.hanghae.degether.WebSocket.model.Message;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//
//import java.util.List;
//
//@EnableJpaRepositories
//public interface MessageJpaRepository extends JpaRepository<Message, Long> {
//    List<FindMessageDto> findAllByRoomId(String roomId);
//    Message findTop1ByRoomIdOrderByCreatedAtDesc(String roomId);
//    Message findByRoomId(String roomId);
//}
