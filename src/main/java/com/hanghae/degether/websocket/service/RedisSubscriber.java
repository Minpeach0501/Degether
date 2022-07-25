package com.hanghae.degether.websocket.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
import com.hanghae.degether.websocket.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;


    /**
     * Redis에서 메시지가 발행(publish)되면 대기하고 있던 onMessage가 해당 메시지를 받아 처리한다.
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {

            // redis에서 발행된 데이터를 받아 deserialize = 역직렬화
            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
            log.info("publishMessage : {}", publishMessage);

            // ChatMessage 객채로 맵핑
            ChatMessageDto roomMessage = objectMapper.readValue(publishMessage, ChatMessageDto.class);

            // Websocket 구독자에게 채팅 메시지 Send
            log.info("roomMessage.getMessage : {}", roomMessage.getMessage());
            log.info("roomMessage.getRoomId : {}", roomMessage.getRoomId());
            log.info("onMessage : {}", roomMessage.getType());
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getRoomId(), roomMessage);

            log.info("메세지 받기도 성공 ");
        } catch (Exception e) {
            throw new CustomException(ErrorCode. FAILED_MESSAGE);
        }
    }
}
