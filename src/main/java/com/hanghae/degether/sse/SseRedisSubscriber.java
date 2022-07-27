package com.hanghae.degether.sse;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
import com.hanghae.degether.websocket.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SseRedisSubscriber {
    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;
    private final RedisTemplate redisTemplate;
    private final NotificationService notificationService;


    /**
     * Redis에서 메시지가 발행(publish)되면 대기하고 있던 onMessage가 해당 메시지를 받아 처리한다.
     */

    public void sendMessage(String message) {
        try {

            // ChatMessage 객채로 맵핑
            NotificationDto.Publish sseMessage = objectMapper.readValue(message, NotificationDto.Publish.class);


            notificationService.send(sseMessage);
            log.info("SSE 메세지 받기도 성공 ");
        } catch (Exception e) {
            throw new CustomException(ErrorCode. FAILED_MESSAGE);
        }
    }
}
