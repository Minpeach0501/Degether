package com.hanghae.degether.sse;


import com.hanghae.degether.project.dto.ResponseDto;
import com.hanghae.degether.project.util.CommonUtil;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.websocket.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * @title 로그인 한 유저 sse 연결
     */
    @GetMapping(value = "/subscribe/{id}", produces = "text/event-stream")
    public SseEmitter subscribe(@PathVariable Long id,
                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        return notificationService.subscribe(id, lastEventId);
    }

    @PutMapping(value = "/api/sse/{notificationId}")
    public ResponseDto<?> sseRead(@PathVariable Long notificationId){
        notificationService.sseRead(notificationId);
        return ResponseDto.builder()
                .ok(true)
                .message("요청 성공")
                .build();
    }
    @DeleteMapping(value = "/api/sse/{notificationId}")
    public ResponseDto<?> sseDelete(@PathVariable Long notificationId){
        notificationService.sseDelete(notificationId);
        return ResponseDto.builder()
                .ok(true)
                .message("삭제 성공")
                .build();
    }
    @GetMapping(value = "/api/sse")
    public ResponseDto<?> sseGet(){
        return ResponseDto.builder()
                .ok(true)
                .message("요청 성공")
                .result(notificationService.sseGet())
                .build();
    }


    // @GetMapping(value = "/ssetest")
    // public void ssetest() {
    //     System.out.println("555555555");
    //     User user = CommonUtil.getUser();
    //     redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>( NotificationDto.Publish.class));
    //     redisTemplate.convertAndSend("sse",
    //             NotificationDto.Publish.builder()
    //                     .content("aaaa")
    //                     .reciverId(user.getId())
    //                     .build());
    //     // notificationService.send(NotificationDto.Publish.builder().content("aaaa").reciver(user).build());
    // }
}