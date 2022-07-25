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

    @GetMapping("/openvidu/api/recordings/start/{sessionId}")
    public ResponseDto<?> startRecording(@PathVariable String sessionId) {
        return openviduService.startRecording(sessionId);
    }

    @GetMapping("/openvidu/api/recordings/stop/{sessionId}")
    public ResponseDto<?> stopRecording(@PathVariable String sessionId) {
        return openviduService.stopRecording(sessionId);
    }
    @GetMapping("/openvidu/api/recordings")
    public ResponseDto<?> listRecording() throws OpenViduJavaClientException, OpenViduHttpException {
        return openviduService.listRecording();
    }
    @GetMapping("/openvidu/api/test")
    public ResponseDto<?> testRecording() throws OpenViduJavaClientException, OpenViduHttpException, IOException {
        return openviduService.testRecording();
    }

    @GetMapping("/api/openvidu/{projectId}")
    public ResponseDto<?> openvidu(HttpServletRequest request, @PathVariable Long projectId ){
        String token = request.getHeader("token");
        return openviduService.openvidu(token, projectId);
    }
}
