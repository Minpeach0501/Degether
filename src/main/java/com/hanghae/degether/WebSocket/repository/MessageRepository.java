package com.hanghae.degether.WebSocket.repository;

import com.hanghae.degether.WebSocket.dto.MessageDto;
import com.hanghae.degether.WebSocket.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Repository
public class MessageRepository {

    private static final String MESSAGE = "MESSAGE";

    private final RedisTemplate<String, Object> redisTemplate; // redisTemplate 사용

    private HashOperations<String, String, List<MessageDto>> opsHashChatMessage;

    @Transactional
    public MessageDto save(MessageDto MessageDto) {

        log.info("chatMessage : {}", MessageDto.getMessage());
        log.info("type: {}", MessageDto.getType());

        //chatMessageDto 를 redis 에 저장하기 위하여 직렬화 한다.
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Message.class));
        String roomId = MessageDto.getRoomId();
        //redis에 저장되어있는 리스트를 가져와, 새로 받아온 chatmessageDto를 더하여 다시 저장한다.
        List<MessageDto> MessageList = opsHashChatMessage.get(MESSAGE, roomId);
        //가져온 List가 null일때 새로운 리스트를 만든다 == 처음에 메세지를 저장할경우 리스트가 없기때문에.
        if (MessageList== null) {
            MessageList = new ArrayList<>();
        }
        MessageList.add(MessageDto);
        //redis 의 hashes 자료구조
        //key : MESSAGE , filed : roomId, value : chatMessageList
        opsHashChatMessage.put(MESSAGE, roomId, MessageList);
        return MessageDto;
    }
}
