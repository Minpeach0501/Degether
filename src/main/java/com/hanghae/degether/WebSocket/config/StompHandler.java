package com.hanghae.degether.WebSocket.config;

import com.hanghae.degether.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        log.info("simpDestination : {}", message.getHeaders().get("simpDestination"));
        log.info("sessionId : {}", message.getHeaders().get("simpSessionId"));
        String sessionId = (String) message.getHeaders().get("simpSessionId");

        // websocket 연결시 헤더의 jwt token 검증
        if (StompCommand.CONNECT == accessor.getCommand()) {
            log.info("CONNECT : {}", sessionId);
            jwtTokenProvider.validateToken(accessor.getFirstNativeHeader("token"));
            // 구독 요청시 유저의 카운트수를 저장하고 최대인원수를 관리하며 , 세션정보를 저장한다.
        }

        return message;
    }
}
