package com.hanghae.degether.websocket.service;


import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
import com.hanghae.degether.project.dto.UserDto;
import com.hanghae.degether.project.util.CommonUtil;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.JwtTokenProvider;
import com.hanghae.degether.websocket.dto.ChatMessageDto;
import com.hanghae.degether.websocket.dto.FindChatMessageDto;
import com.hanghae.degether.websocket.dto.SenderDto;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {
    //의존성 주입
    private static final String CHAT_MESSAGE = "CHAT_MESSAGE";
    private final RedisPublisher redisPublisher;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageRepository messageRepository;
    private final RedisMessageRepository redisMessageRepository;
    @Transactional
    public void save(ChatMessageDto messageDto, String token) {
        log.info("save Message : {}", messageDto.getMessage());

        // 유저 정보값을 토큰으로 찾아오기
        User user = CommonUtil.getUserByToken(token, jwtTokenProvider);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));

        // 메세지 보내는 사람의 정보값 넣기
        messageDto.setUser(SenderDto.builder()
                        .id(user.getId())
                        .profileUrl(user.getProfileUrl())
                        .role(user.getRole())
                        .nickname(user.getNickname())
                .build());
        messageDto.setCreatedAt(now);

        // DB 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .message(messageDto.getMessage())
                .projectId(messageDto.getProjectId())
                .type(messageDto.getType())
                .user(user)
                .createdAt(now)
                .build();

        messageRepository.save(chatMessage);
        messageDto.setId(chatMessage.getId());
        redisPublisher.publishsave(messageDto);

    }

    // 이전 채팅 기록 조회
    public List<FindChatMessageDto> getAllMessage(String projectId) {
        HashOperations<String, String, List<FindChatMessageDto>> opsHashChatMessage = redisTemplate.opsForHash();


        List<FindChatMessageDto> chatMessageDtoList = opsHashChatMessage.get(CHAT_MESSAGE, projectId);

        if (chatMessageDtoList!= null && chatMessageDtoList.size() > 10) {
            //from redis
            return chatMessageDtoList;
        }
        //from mysql

        // redis 에서 가져온 메세지 리스트의 값이 null 일때  Mysql db에서 데이터를 불러와 레디스에 저장후 리턴
        List<FindChatMessageDto> chatMessages = messageRepository.findTop10ByProjectIdOrderByCreatedAtDesc(projectId);

        // ChatRoom chatRoom2 = roomRepository.findByRoomId(projectId);
        redisTemplate.opsForHash().put(CHAT_MESSAGE, projectId, chatMessages);
        redisTemplate.expire(CHAT_MESSAGE,10, TimeUnit.SECONDS);

        return chatMessages;
    }


}



