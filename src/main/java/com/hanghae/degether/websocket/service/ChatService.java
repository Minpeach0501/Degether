package com.hanghae.degether.websocket.service;


import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
import com.hanghae.degether.project.repository.ProjectRepository;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.JwtTokenProvider;
import com.hanghae.degether.websocket.dto.ChatMessageDto;
import com.hanghae.degether.websocket.model.ChatMessage;
import com.hanghae.degether.websocket.model.ChatRoom;
import com.hanghae.degether.websocket.repository.ChatMessageRepository;
import com.hanghae.degether.websocket.repository.ChatRoomRepository;
import com.hanghae.degether.websocket.repository.MessageRepository;
import com.hanghae.degether.websocket.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final RedisPublisher redisPublisher;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final ProjectRepository projectRepository;


    @Transactional
    public void save(ChatMessageDto messageDto, String token) {
        log.info("save Message : {}", messageDto.getMessage());

        // 유저 정보값을 토큰으로 찾아오기
        String username = jwtTokenProvider.getUserPk(token);
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_EXIST_USER)
        );

        //date type 을 string으로 형변환시킨다. 시간표현
        DateFormat dateFormat = new SimpleDateFormat("dd,MM,yyyy,HH,mm,ss", Locale.KOREA);
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(calendar.getTimeInMillis());
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String dateToStr = dateFormat.format(date);


        // 메세지 보내는 사람의 정보값 넣기
        messageDto.setSender(user.getNickName());
        messageDto.setProfileUrl(user.getProfileUrl());
        messageDto.setCreatedAt(dateToStr);
        log.info("type : {}", messageDto.getType());


        //받아온 메세지의 타입이 ENTER 일때 알림 메세지와 함께 채팅방 입장 !
        if (ChatMessage.MessageType.ENTER.equals(messageDto.getType())) {
            chatRoomRepository.enterChatRoom(messageDto.getRoomId());
            messageDto.setMessage("[알림] " + messageDto.getSender() + "님이 입장하셨습니다.");
            String roomId = messageDto.getRoomId();
        }
        log.info("ENTER : {}", messageDto.getMessage());
//        ChatRoom chatRoom = roomRepository.findByUsername(username);
        ChatRoom chatRoom = roomRepository.findByRoomId(messageDto.getRoomId());
        if (chatRoom == null) {
            chatRoom = ChatRoom.create(messageDto.getRoomId());
            roomRepository.save(chatRoom);
        }

        //캐시 저장
        chatMessageRepository.save(messageDto);
        ChatMessage chatMessage = new ChatMessage(messageDto,chatRoom);

       // DB 저장
        messageRepository.save(chatMessage);


        // Websocket 에 발행된 메시지를 redis 로 발행한다(publish)
        redisPublisher.publish(ChatRoomRepository.getTopic(messageDto.getRoomId()), messageDto);
    }

    //redis에 저장되어있는 message 들 출력
    public List<ChatMessageDto> getMessages(String roomId) {
        log.info("getMessages roomId : {}", roomId);
        return chatMessageRepository.findAllMessage(roomId);
    }
}

