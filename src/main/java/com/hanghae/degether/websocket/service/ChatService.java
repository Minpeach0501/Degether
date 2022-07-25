package com.hanghae.degether.websocket.service;


import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.JwtTokenProvider;
import com.hanghae.degether.websocket.dto.ChatMessageDto;
import com.hanghae.degether.websocket.dto.FindChatMessageDto;
import com.hanghae.degether.websocket.model.ChatMessage;
import com.hanghae.degether.websocket.model.ChatRoom;
import com.hanghae.degether.websocket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {
    //의존성 주입
    private final RedisPublisher redisPublisher;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final RedisMessageRepository redisMessageRepository;
    private final RedisRoomRepository redisRoomRepository;


    //redis 관련
    public static final String ENTER_INFO = "ENTER_INFO"; // 채팅룸에 입장한 클라이언트의 sessionId와 채팅룸 id를 맵핑한 정보 저장
    private static final String CHAT_MESSAGE = "CHAT_MESSAGE"; // 채팅룸에 메세지들을 저장
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, String> hashOpsEnterInfo; // Redis 의 Hashes 사용
    private HashOperations<String, String, List<ChatMessageDto>> opsHashChatMessage; // Redis 의 Hashes 사용
    // 채팅방의 대화 메시지를 발행하기 위한 redis topic 정보. 서버별로 채팅방에 매치되는 topic정보를 Map에 넣어 roomId로 찾을수 있도록 한다.
    private static Map<String, ChannelTopic> topics;
    private HashOperations<String, String, ChatRoom> opsHashChatRoom;
    private final RedisMessageListenerContainer redisMessageListener;
    private final RedisSubscriber redisSubscriber;
    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
        opsHashChatMessage = redisTemplate.opsForHash();
        hashOpsEnterInfo = redisTemplate.opsForHash();
        topics = new HashMap<>();
    }

    @Transactional
    public void save(ChatMessageDto messageDto, String token) {
        log.info("save Message : {}", messageDto.getMessage());
        log.info(token);

        // 유저 정보값을 토큰으로 찾아오기
        String username = jwtTokenProvider.getUserPk(token);
        log.info(username);
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_EXIST_USER)
        );

        String profileUrl = user.getProfileUrl();
        log.info(profileUrl);

        String nickName = user.getNickname();
        log.info(nickName);


        //date type 을 string으로 형변환시킨다. 시간표현
        DateFormat dateFormat = new SimpleDateFormat("dd,MM,yyyy,HH,mm,ss", Locale.KOREA);
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(calendar.getTimeInMillis());
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String dateToStr = dateFormat.format(date);

        // 메세지 보내는 사람의 정보값 넣기
        messageDto.setSender(nickName);
        messageDto.setProfileUrl(user.getProfileUrl());
        messageDto.setCreatedAt(dateToStr);
        messageDto.setUserId(user.getId());
        messageDto.setMessage(messageDto.getMessage());

        log.info("sender: {}", messageDto.getSender());
        log.info("type : {}", messageDto.getType());

        ChatRoom chatRoom = roomRepository.findByRoomId(messageDto.getRoomId());
        log.info(String.valueOf(chatRoom));


        chatRoom = createChatRoom(messageDto);


        //받아온 메세지의 타입이 ENTER 일때 알림 메세지와 함께 채팅방 입장 !
        if (ChatMessage.MessageType.ENTER.equals(messageDto.getType())) {
           // 토픽 가져오는 메서드 사용
            getTopic(messageDto);

            messageDto.setMessage("[알림] " + messageDto.getSender() + "님이 입장하셨습니다.");
        }
        log.info("ENTER : {}", messageDto.getMessage());


        // Websocket 에 발행된 메시지를 redis 로 발행한다(publish)
        redisPublisher.publishsave(messageDto);

        //캐시 저장후 entity 값 설정
        ChatMessage chatMessage = new ChatMessage(messageDto, chatRoom);

        // DB 저장
        messageRepository.save(chatMessage);
    }

    // 이전 채팅 기록 조회
    public List<ChatMessageDto> getAllMessage(String roomId) {

        List<ChatMessageDto> chatMessageDtoList = new ArrayList<>();


        chatMessageDtoList = redisMessageRepository.findAllMessageByRoomId(roomId).stream().map(ChatMessageDto::new).collect(Collectors.toList());
        log.info(chatMessageDtoList.toString());
        //chatMessage 리스트를 불러올때, 리스트의 사이즈가 0보다 크면 redis 정보를 가져온다
        //redis 에서 가져온 리스트의 사이즈가  0보다 크다 == 저장된 정보가 있다.
        if (chatMessageDtoList.size() > 0) {
            return chatMessageDtoList;
        }

        // redis 에서 가져온 메세지 리스트의 값이 null 일때  Mysql db에서 데이터를 불러와 레디스에 저장후 리턴
        List<FindChatMessageDto> chatMessages = messageRepository.findAllByRoomId(roomId);

        ChatRoom chatRoom2 = roomRepository.findByRoomId(roomId);

        for (FindChatMessageDto chatMessage : chatMessages) {
            ChatMessageDto chatMessageDto = new ChatMessageDto(chatMessage);
            chatMessageDtoList.add(chatMessageDto);


            ChatMessage chatMessage1 = new ChatMessage(chatMessageDto, chatRoom2);
            redisMessageRepository.save(chatMessage1);
        }



        return chatMessageDtoList;
    }


    //토픽 가져오는것 메서드 추출.
    private void getTopic(ChatMessageDto messageDto) {
        String roomId = messageDto.getRoomId();
        log.info(roomId);
        //enterChatroom 채팅방 들어가는 로직
        ChannelTopic topic = topics.get(roomId);
        if (topic == null) {
            topic = new ChannelTopic(roomId);
            log.info(topic.toString());
            redisMessageListener.addMessageListener(redisSubscriber, topic);
            log.info("메세지 리스너 작동확인 ");

            topics.put(roomId, topic);
        }
    }

    // 채팅방생성 메서드 추출
    private ChatRoom createChatRoom(ChatMessageDto messageDto) {

        ChatRoom chatRoom = roomRepository.findByRoomId(messageDto.getRoomId());

        // 채팅방이 없을때 생성 하는 곳
        if (chatRoom == null || chatRoom.equals("null")) {
            ChatRoom chatRoom2 = ChatRoom.create(messageDto.getRoomId());
            // redis 저장
            redisRoomRepository.save(chatRoom2);
//            opsHashChatRoom.put(CHAT_ROOMS, roomId, chatRoom);
            // DB 저장
            roomRepository.save(chatRoom2);

            chatRoom = chatRoom2;
        }



        log.info(String.valueOf(chatRoom));
        return chatRoom;
    }
    // 구독 요청시
    public void setUserEnterInfo(String roomId, String sessionId) {
        hashOpsEnterInfo.put(ENTER_INFO, sessionId, roomId);
        log.info("hashPosEnterInfo.put : {}", hashOpsEnterInfo.get(ENTER_INFO, sessionId));
    }

    // 구독 취소하거나 or 세션연결이 끊겼을 시
    public void removeUserEnterInfo(String sessionId, String roomId) {
        hashOpsEnterInfo.delete(ENTER_INFO, sessionId, roomId);
        log.info("hashPosEnterInfo.put : {}", hashOpsEnterInfo.get(ENTER_INFO, sessionId));
    }

}



