package com.hanghae.degether.openvidu.controller;

import com.hanghae.degether.openvidu.service.OpenviduService;
import com.hanghae.degether.openvidu.service.SttService;
import com.hanghae.degether.project.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SttController {
    private final OpenviduService openviduService;
    @GetMapping("/api/meetingNotes/{projectId}")
    public ResponseDto<?> getMeetingNotes(@PathVariable Long projectId){
        return ResponseDto.builder()
                .ok(true)
                .message("요청 성공")
                .result(openviduService.getMeetingNotes(projectId))
                .build();
    }
    @GetMapping("/api/meetingNote/{meetingNoteId}")
    public ResponseDto<?> getMeetingNote(@PathVariable Long meetingNoteId){
        return ResponseDto.builder()
                .ok(true)
                .message("요청 성공")
                .result(openviduService.getMeetingNote(meetingNoteId))
                .build();
    }
}
