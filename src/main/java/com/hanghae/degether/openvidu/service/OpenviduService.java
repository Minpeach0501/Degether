package com.hanghae.degether.openvidu.service;

import com.hanghae.degether.project.dto.ResponseDto;
import io.openvidu.java.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OpenviduService {
    private OpenVidu openVidu;
    private String OPENVIDU_URL;
    // Secret shared with our OpenVidu server
    private String SECRET;
    private Map<String, String> sessionRecordingMap = new ConcurrentHashMap<>();
    public OpenviduService(@Value("${openvidu.secret}") String secret, @Value("${openvidu.url}") String openviduUrl) {
        this.SECRET = secret;
        this.OPENVIDU_URL = openviduUrl;
        this.openVidu = new OpenVidu(OPENVIDU_URL, SECRET);
    }

    public ResponseDto<?> startRecording(String sessionId)  {
        if (sessionRecordingMap.get(sessionId) != null) {
            try{
                Recording recording = openVidu.getRecording(sessionRecordingMap.get(sessionId));
                if (recording.getStatus().equals(Recording.Status.started)) {
                    //녹음중
                    return ResponseDto.builder()
                            .ok(true)
                            .message("녹음중 입니다.")
                            .result(recording)
                            .build();
                } else if (recording.getStatus().equals(Recording.Status.ready)) {
                    //녹음 완료, 녹음종료 처리가 안됨
                }
                return ResponseDto.builder().message("이미 녹음중 입니다").build();
            }catch (OpenViduJavaClientException | OpenViduHttpException exception){
                return ResponseDto.builder()
                        .ok(false)
                        .message("화상채팅 서버 오류 입니다.")
                        .build();
            }
        }
        //녹음 시작
        try{
            RecordingProperties properties = new RecordingProperties.Builder()
                    .outputMode(Recording.OutputMode.COMPOSED)
                    .hasAudio(true)
                    .hasVideo(false)
                    .build();
            Recording recording = openVidu.startRecording(sessionId, properties);
            sessionRecordingMap.put(sessionId, recording.getId());
            return ResponseDto.builder()
                    .ok(true)
                    .message("녹음 시작")
                    .result(recording)
                    .build();

        }catch (OpenViduJavaClientException | OpenViduHttpException exception){
            sessionRecordingMap.remove(sessionId);
            return ResponseDto.builder()
                    .ok(false)
                    .message("화상채팅 서버 오류 입니다.")
                    .build();
        }
    }
    public ResponseDto<?> stopRecording(String sessionId, String recordingId)  {
        Recording recording = openVidu.stopRecording(recordingId);
    }
}
