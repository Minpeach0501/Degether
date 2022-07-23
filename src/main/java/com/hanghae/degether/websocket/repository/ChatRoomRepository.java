package com.hanghae.degether.websocket.repository;


import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.project.repository.ProjectRepository;
import com.hanghae.degether.websocket.dto.UserInfoDto;
import com.hanghae.degether.websocket.model.ChatRoom;
import com.hanghae.degether.websocket.service.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class ChatRoomRepository {

    // 채팅방(topic)에 발행되는 메시지를 처리할 Listner
    private final RedisMessageListenerContainer redisMessageListener;
    private final RedisSubscriber redisSubscriber;
    private final RoomRepository roomRepository;
    private final ProjectRepository projectRepository;
    private final MessageRepository messageRepository;

    // Redis
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, ChatRoom> opsHashChatRoom;

    // 채팅방의 대화 메시지를 발행하기 위한 redis topic 정보. 서버별로 채팅방에 매치되는 topic정보를 Map에 넣어 roomId로 찾을수 있도록 한다.
    private static Map<String, ChannelTopic> topics;

    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
        topics = new HashMap<>();
        topics.put("57", new ChannelTopic("chatroom"));
    }


    //채팅방 입장 : redis에 topic을 만들고 pub/sub 통신을 하기 위해 리스너를 설정한다.

    public void enterChatRoom(String roomId) {
        ChannelTopic topic = topics.get(roomId);
        if (topic == null) {
            topic = new ChannelTopic(roomId);
            redisMessageListener.addMessageListener(redisSubscriber, topic);
            topics.put(roomId, topic);
        }
    }


     // 채팅방 생성 , 프로젝트 생성시 만들어진 projectid를 받아와서  roomid로 사용한다.

    @Transactional
    public void createChatRoom(Project project, UserInfoDto userDto) {

        ChatRoom chatRoom = ChatRoom.create(project, userDto);

        // redis 저장
        opsHashChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);

        // DB 저장
        roomRepository.save(chatRoom);
    }

    public static ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }

}