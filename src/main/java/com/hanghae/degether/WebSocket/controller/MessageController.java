package com.hanghae.degether.WebSocket.controller;

import com.hanghae.degether.WebSocket.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final SimpMessageSendingOperations sendingOperations;

    @MessageMapping("/chat/message")
    public void enter(Message message) {
        //입장시
        if (Message.MessageType.ENTER.equals(message.getType())) {
            message.setMessage(message.getNickname()+"님이 입장하였습니다.");
        }
        // 퇴장시
        if (Message.MessageType.QUIT.equals(message.getType())){
            message.setMessage(message.getNickname() + " 님이 퇴장하였습니다.");
        }

        sendingOperations.convertAndSend("/ws/chat/room/"+message.getRoomId(),message);
    }



}
