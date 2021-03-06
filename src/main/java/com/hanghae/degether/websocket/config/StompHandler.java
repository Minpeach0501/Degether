package com.hanghae.degether.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanghae.degether.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        log.info(String.valueOf(message));

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        String sessionId = (String) message.getHeaders().get("simpSessionId");

        log.info("simpDestination : {}", message.getHeaders().get("simpDestination"));
        log.info("sessionId : {}", sessionId);

        // websocket 연결시 헤더의 security를 통해 jwt token 검증
        if (StompCommand.CONNECT == accessor.getCommand()) {

            log.info("CONNECT : {}", sessionId);
            jwtTokenProvider.validateToken(accessor.getFirstNativeHeader("Authorization"));

            // 구독 요청시 유저 세션정보를 저장한다.
        } else if (StompCommand.SUBSCRIBE == accessor.getCommand()) {

            // 채팅방 나간 유저를 방에서 세션정보를 지움
        } else if (StompCommand.UNSUBSCRIBE == accessor.getCommand() || StompCommand.DISCONNECT == accessor.getCommand()) {

        }
        return message;
    }
}
