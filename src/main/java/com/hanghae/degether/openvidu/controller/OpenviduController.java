package com.hanghae.degether.openvidu.controller;

import com.hanghae.degether.openvidu.service.OpenviduService;
import com.hanghae.degether.project.dto.ResponseDto;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class OpenviduController {
    private final OpenviduService openviduService;

    //녹음 시작
    @GetMapping("/openvidu/api/recordings/start/{sessionId}")
    public ResponseDto<?> startRecording(@PathVariable String sessionId) {
        return openviduService.startRecording(sessionId);
    }
    //녹음 중지
    @GetMapping("/openvidu/api/recordings/stop/{sessionId}")
    public ResponseDto<?> stopRecording(@PathVariable String sessionId) {
        return openviduService.stopRecording(sessionId);
    }
    //녹음 목록
    @GetMapping("/openvidu/api/recordings")
    public ResponseDto<?> listRecording() throws OpenViduJavaClientException, OpenViduHttpException {
        return openviduService.listRecording();
    }
    //openvidu 서버에서 토큰을 통해 참여 가능 여부 판별
    @GetMapping("/api/openvidu/{projectId}")
    public ResponseDto<?> openvidu(HttpServletRequest request, @PathVariable Long projectId ){
        String token = request.getHeader("Authorization");
        return openviduService.openvidu(token, projectId);
    }
}
