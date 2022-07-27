package com.hanghae.degether.websocket.service;


import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
import com.hanghae.degether.project.util.CommonUtil;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.JwtTokenProvider;
import com.hanghae.degether.websocket.dto.ChatMessageDto;
import com.hanghae.degether.websocket.dto.FindChatMessageDto;
import com.hanghae.degether.websocket.model.ChatMessage;
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
import java.time.LocalDate;
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
    private final RedisMessageRepository redisMessageRepository;
    @Transactional
    public void save(ChatMessageDto messageDto, String token) {
        log.info("save Message : {}", messageDto.getMessage());

        // 유저 정보값을 토큰으로 찾아오기
        User user = CommonUtil.getUserByToken(token, jwtTokenProvider);


        // 메세지 보내는 사람의 정보값 넣기
        messageDto.setSender(nickName);
        messageDto.setProfileUrl(user.getProfileUrl());
        messageDto.setCreatedAt(LocalDate.now());
        messageDto.setUserId(user.getId());
        messageDto.setMessage(messageDto.getMessage());


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

}



