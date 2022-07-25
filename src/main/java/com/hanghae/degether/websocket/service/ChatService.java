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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final RedisPublisher redisPublisher;
    private final ChatRoomRepository chatRoomRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final RedisMessageRepository redisMessageRepository;

    private static final String CHAT_MESSAGE = "CHAT_MESSAGE"; // 채팅룸에 메세지들을 저장
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, List<ChatMessageDto>> opsHashChatMessage; // Redis 의 Hashes 사용

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

        log.info("type : {}", messageDto.getType());

        ChatRoom chatRoom = roomRepository.findByRoomId(messageDto.getRoomId());
        log.info(String.valueOf(chatRoom));


        // 값 들어오는지 확인용
        String profileUrl2 = messageDto.getProfileUrl();
        String nickName2 = messageDto.getSender();
        String createdAt = messageDto.getCreatedAt();
        String roomId1 = messageDto.getRoomId();
        String sender = messageDto.getSender();

        log.info(nickName2);
        log.info(profileUrl2);
        log.info(createdAt);
        log.info(roomId1);
        log.info(sender);

        if (chatRoom == null || chatRoom.equals("null")) {
            ChatRoom chatRoom1;
            chatRoom1 = chatRoomRepository.createChatRoom(messageDto.getRoomId());
            roomRepository.save(chatRoom1);
            chatRoom = chatRoom1;
        }

        log.info(String.valueOf(chatRoom));


        //받아온 메세지의 타입이 ENTER 일때 알림 메세지와 함께 채팅방 입장 !
        if (ChatMessage.MessageType.ENTER.equals(messageDto.getType())) {
            chatRoomRepository.enterChatRoom(messageDto.getRoomId());
            messageDto.setMessage("[알림] " + messageDto.getSender() + "님이 입장하셨습니다.");
            String roomId = messageDto.getRoomId();
        }
        log.info("ENTER : {}", messageDto.getMessage());
//        ChatRoom chatRoom = roomRepository.findByUsername(username);


        //캐시 저장
//        chatMessageRepository.save(messageDto);

//        //캐시 저장후 entity 값 설정
//        ChatMessage chatMessage = new ChatMessage(messageDto,chatRoom);
//
//       // DB 저장
//        messageRepository.save(chatMessage);


        // Websocket 에 발행된 메시지를 redis 로 발행한다(publish)
        redisPublisher.publishsave(ChatRoomRepository.getTopic(messageDto.getRoomId()), messageDto);


        //캐시 저장후 entity 값 설정
        ChatMessage chatMessage = new ChatMessage(messageDto, chatRoom);

        // DB 저장
        messageRepository.save(chatMessage);
    }


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


}



