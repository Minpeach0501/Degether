package com.hanghae.degether.websocket.controller;


import com.hanghae.degether.websocket.dto.ChatMessageDto;
import com.hanghae.degether.websocket.dto.FindChatMessageDto;
import com.hanghae.degether.websocket.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor

@Slf4j
public class AllChatController {
    private final ChatService chatService;


    // 메세지 보내기
    @MessageMapping({"/chat/message"})
    public void message(ChatMessageDto messageDto, @Header("Authorization") String token) {
        log.info("요청 메서드 [message] /chat/message");
        chatService.save(messageDto, token);
    }

    //이전에 채팅 기록  조회
    @GetMapping("/chat/message/{projectId}")
    @ResponseBody
    public  List<FindChatMessageDto> getAllMessage(@PathVariable String projectId){
        return chatService.getAllMessage(projectId);
    }



}
