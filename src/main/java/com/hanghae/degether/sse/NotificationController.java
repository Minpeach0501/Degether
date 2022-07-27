package com.hanghae.degether.sse;

import com.hanghae.degether.project.util.CommonUtil;
import com.hanghae.degether.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
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
        System.out.println("11111111");
        return notificationService.subscribe(id, lastEventId);
    }
    @GetMapping(value = "/ssetest")
    public void ssetest() {
        System.out.println("555555555");
        User user = CommonUtil.getUser();
        // redisTemplate.convertAndSend("sse",NotificationDto.Publish.builder().content("aaaa").reciver(user).build());
        notificationService.send(NotificationDto.Publish.builder().content("aaaa").reciver(user).build());
    }
}