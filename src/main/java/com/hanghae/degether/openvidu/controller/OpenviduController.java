package com.hanghae.degether.openvidu.controller;

import com.hanghae.degether.openvidu.service.OpenviduService;
import com.hanghae.degether.project.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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
}
