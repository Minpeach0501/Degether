package com.hanghae.degether.websocket.service;

import com.hanghae.degether.websocket.dto.ChatMessageDto;
import com.hanghae.degether.websocket.model.ChatMessage;
import com.hanghae.degether.websocket.model.ChatRoom;
import com.hanghae.degether.websocket.repository.RedisMessageRepository;
import com.hanghae.degether.websocket.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisPublisher {

    private static final String CHAT_MESSAGE = "CHAT_MESSAGE"; // 채팅룸에 메세지들을 저장
    public static final String USER_COUNT = "USER_COUNT"; // 채팅룸에 입장한 클라이언트수 저장
    public static final String ENTER_INFO = "ENTER_INFO"; // 채팅룸에 입장한 클라이언트의 sessionId와 채팅룸 id를 맵핑한 정보 저장

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageRepository redisMessageRepository;
    private final RoomRepository roomRepository;


    private final StringRedisTemplate stringRedisTemplate; // StringRedisTemplate 사용
    private HashOperations<String, String, String> hashOpsEnterInfo; // Redis 의 Hashes 사용
    private HashOperations<String, String, List<ChatMessageDto>> opsHashChatMessage; // Redis 의 Hashes 사용
    private ValueOperations<String, String> valueOps; // Redis 의 String 구조 사용
    @PostConstruct
    private void init() {
        opsHashChatMessage = redisTemplate.opsForHash();
        hashOpsEnterInfo = redisTemplate.opsForHash();
        valueOps = stringRedisTemplate.opsForValue();
    }


    // websocket 에서 받아온 메세지를 convertAndsend를 통하여 Redis의 메세지 리스너로 발행
    // redisrepository 를 이용해 저장
    public void publishsave(ChannelTopic topic, ChatMessageDto messageDto){

        ChannelTopic topic1 = new ChannelTopic(messageDto.getRoomId());

        ChatRoom chatRoom = roomRepository.findByRoomId(messageDto.getRoomId());

        ChatMessage chatMessage = new ChatMessage(messageDto,chatRoom);

        //chatMessageDto 를 redis 에 저장하기 위하여 직렬화 한다.
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatMessage.class));
        String roomId = messageDto.getRoomId();

        //redis에 저장되어있는 리스트를 가져와, 새로 받아온 chatmessageDto를 더하여 다시 저장한다.
        List<ChatMessageDto> chatMessageList = opsHashChatMessage.get(CHAT_MESSAGE, roomId);

        //가져온 List가 null일때 새로운 리스트를 만든다 == 처음에 메세지를 저장할경우 리스트가 없기때문에.
        if (chatMessageList == null) {
            chatMessageList = new ArrayList<>();
        }
        chatMessageList.add(messageDto);


        //redis 의 hashes 자료구조 ---->> key : CHAT_MESSAGE , filed : roomId, value : chatMessageList
        opsHashChatMessage.put(CHAT_MESSAGE, roomId, chatMessageList);
        redisTemplate.expire(CHAT_MESSAGE,30, TimeUnit.MINUTES);
        redisMessageRepository.save(chatMessage);

        redisTemplate.convertAndSend(topic1.getTopic(), messageDto);


    }

}