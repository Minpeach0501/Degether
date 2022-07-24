package com.hanghae.degether.websocket.controller;


import com.hanghae.degether.websocket.dto.ChatMessageDto;
import com.hanghae.degether.websocket.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final ChatService chatService;
    private final SimpMessageSendingOperations sendingOperations;


    // 메세지 보내기
    @MessageMapping({"/chat/message"})
    public void message(ChatMessageDto message, @Header("Authorization") String token) {
        log.info("요청 메서드 [message] /chat/message");
            chatService.save(message, token);
    }

//    //이전에 채팅 기록들 모두 조회
//    @GetMapping("/chat/message/{roomId}")
//    @ResponseBody
//    public List<ChatMessageDto> getMessage(@PathVariable String roomId) {
//        log.info("요청 메서드 [GET] /chat/message/{roomId}");
//        return chatService.getMessages(roomId);
//    }



}
