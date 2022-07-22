package com.hanghae.degether.openvidu.service;

import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
import com.hanghae.degether.openvidu.dto.VitoResponseDto;
import com.hanghae.degether.openvidu.model.MeetingNote;
import com.hanghae.degether.openvidu.model.Utterance;
import com.hanghae.degether.openvidu.repository.MeetingNoteRepository;
import com.hanghae.degether.project.dto.ResponseDto;
import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.project.repository.ProjectRepository;
import com.hanghae.degether.project.repository.UserProjectRepository;
import com.hanghae.degether.project.util.CommonUtil;
import com.hanghae.degether.user.model.User;
import io.openvidu.java.client.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenviduService {
    private final SttService sttService;
    private final MeetingNoteRepository meetingNoteRepository;
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    @Value("${openvidu.url}")
    private String OPENVIDU_URL;
    // Secret shared with our OpenVidu server
    @Value("${openvidu.secret}")
    private String SECRET;
    private OpenVidu openVidu;
    private Map<String, String> sessionRecordingMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        this.openVidu = new OpenVidu(OPENVIDU_URL, SECRET);
    }
    public ResponseDto<?> startRecording(String sessionId)  {
        System.out.println(sessionRecordingMap.toString());
        Project project = CommonUtil.getProject(Long.parseLong(sessionId), projectRepository);
        User user = CommonUtil.getUser();
        if (!userProjectRepository.existsByProjectAndUserAndIsTeam(project, user, true)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
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
                // System.out.println(exception.getMessage());
                return ResponseDto.builder()
                        .ok(false)
                        .message("화상채팅 서버 오류 입니다.")
                        .build();
            }
        }

        //녹음 시작
        try{
            Recording recording = openVidu.getRecording(sessionId);
            if(recording!=null && "started".equals(recording.getStatus())){
                //녹음중임
            }else{
                RecordingProperties properties = new RecordingProperties.Builder()
                        .outputMode(Recording.OutputMode.COMPOSED)
                        .hasAudio(true)
                        .hasVideo(false)
                        .build();
                recording = openVidu.startRecording(sessionId, properties);
            }
            sessionRecordingMap.put(sessionId, recording.getId());
            return ResponseDto.builder()
                    .ok(true)
                    .message("녹음 시작")
                    .result(recording)
                    .build();

        }catch (OpenViduJavaClientException | OpenViduHttpException exception){
            sessionRecordingMap.remove(sessionId);
            System.out.println(exception.getMessage());
            return ResponseDto.builder()
                    .ok(false)
                    .message("화상채팅 서버 오류 입니다.")
                    .build();
        }
    }
    @Transactional
    public ResponseDto<?> stopRecording(String sessionId)  {
        System.out.println(sessionRecordingMap.toString());
        Project project = CommonUtil.getProject(Long.parseLong(sessionId), projectRepository);
        User user = CommonUtil.getUser();
        if (!userProjectRepository.existsByProjectAndUserAndIsTeam(project, user, true)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        try {
            Recording recording = openVidu.stopRecording(sessionRecordingMap.get(sessionId));
            sessionRecordingMap.remove(sessionId);
            String sttId = sttService.getSttId(recording.getUrl(), true);
            MeetingNote meetingNote = MeetingNote.builder()
                    .sttId(sttId)
                    .createdAt(recording.getCreatedAt())
                    .duration((long) recording.getDuration())
                    .url(recording.getUrl())
                    .status(false)
                    .project(project)
                    .build();
            MeetingNote savedMeetingNote = meetingNoteRepository.save(meetingNote);
            getSttUtterance(sttId, savedMeetingNote);
            return ResponseDto.builder()
                    .ok(true)
                    .message("회의록 저장 성공")
                    .build();

        } catch (OpenViduJavaClientException | OpenViduHttpException exception) {
            sessionRecordingMap.remove(sessionId);
            System.out.println(exception);
            return ResponseDto.builder()
                    .ok(false)
                    .message("화상채팅 서버 오류 입니다.")
                    .build();
        } catch (IOException e) {
            System.out.println(e);
            return ResponseDto.builder()
                    .ok(false)
                    .message("STT 서버 오류 입니다.")
                    .build();
        }

    }

    public void getSttUtterance(String sttId, MeetingNote savedMeetingNote) {
        VitoResponseDto vitoResponseDto = sttService.getSttUtterance(sttId, true);
        if ("completed".equals(vitoResponseDto.getStatus())) {
            //stt 완료
            List<Utterance> utterances = vitoResponseDto.getResults().getUtterances().stream().map(utterance -> Utterance.builder()
                    .start_at(utterance.getStart_at())
                    .duration(utterance.getDuration())
                    .msg(utterance.getMsg())
                    .spk(utterance.getSpk())
                    .build()).collect(Collectors.toList());
            savedMeetingNote.updateUtterances(utterances);
        }
    }
    public ResponseDto<?> listRecording() throws OpenViduJavaClientException, OpenViduHttpException {

        return ResponseDto.builder().result(openVidu.listRecordings()).build();
    }
    public ResponseDto<?> testRecording() throws OpenViduJavaClientException, OpenViduHttpException, IOException {

        String sttId = sttService.getSttId("https://vidutest.shop/openvidu/recordings/ses_LPtXhnb6h8/ses_LPtXhnb6h8.webm", true);
        VitoResponseDto vitoResponseDto = sttService.getSttUtterance(sttId, true);
        return ResponseDto.builder().result(openVidu.listRecordings()).build();
    }
}
