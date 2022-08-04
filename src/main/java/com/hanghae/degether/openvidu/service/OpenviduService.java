package com.hanghae.degether.openvidu.service;

import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
import com.hanghae.degether.openvidu.dto.MeetingNoteDto;
import com.hanghae.degether.openvidu.dto.UtteranceDto;
import com.hanghae.degether.openvidu.dto.VitoResponseDto;
import com.hanghae.degether.openvidu.model.Meetingnote;
import com.hanghae.degether.openvidu.model.Utterance;
import com.hanghae.degether.openvidu.repository.MeetingNoteRepository;
import com.hanghae.degether.openvidu.repository.UtteranceRepository;
import com.hanghae.degether.project.dto.ResponseDto;
import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.project.repository.ProjectRepository;
import com.hanghae.degether.project.repository.UserProjectRepository;
import com.hanghae.degether.project.util.CommonUtil;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.JwtTokenProvider;
import io.openvidu.java.client.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenviduService {
    private final SttService sttService;
    private final MeetingNoteRepository meetingNoteRepository;
    private final ProjectRepository projectRepository;
    private final UtteranceRepository utteranceRepository;
    private final UserProjectRepository userProjectRepository;
    private final JwtTokenProvider tokenProvider;
    @Value("${openvidu.url}")
    private String OPENVIDU_URL;
    // Secret shared with our OpenVidu server
    @Value("${openvidu.secret}")
    private String SECRET;
    private OpenVidu openVidu;
    private Map<String, String> sessionRecordingMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        //openvidu 서버와 연결
        this.openVidu = new OpenVidu(OPENVIDU_URL, SECRET);
    }
    //녹음 시작
    public ResponseDto<?> startRecording(String sessionId)  {
        //프로젝트, 유저 유효성 판별
        Project project = CommonUtil.getProject(Long.parseLong(sessionId), projectRepository);
        User user = CommonUtil.getUser();
        if (!userProjectRepository.existsByProjectAndUserAndIsTeam(project, user, true)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        // 이미 녹음 중일때
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
                }
                return ResponseDto.builder().message("이미 녹음중 입니다").build();
            }catch (OpenViduJavaClientException | OpenViduHttpException exception){
                return ResponseDto.builder()
                        .ok(false)
                        .message("화상채팅 서버 오류 입니다.")
                        .build();
            }
        }

        //새로운 녹음 시작
        Recording recording = null;
        try{
            //openvidu 서버의 녹음 데이터 확인
            recording = openVidu.getRecording(sessionId);
        }catch (OpenViduJavaClientException | OpenViduHttpException exception){
            log.info("not current Recording");
        }
        try{
            if(recording!=null && "started".equals(recording.getStatus())){
                //openvidu 서버의 녹음 데이터 확인
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
            //녹음 시작 실패
            sessionRecordingMap.remove(sessionId);
            throw new CustomException(ErrorCode.OPENVIDU_ERROR);
        }
    }
    //녹음 중지
    @Transactional
    public ResponseDto<?> stopRecording(String sessionId)  {
        //프로젝트, 유저 유효성 판별
        Project project = CommonUtil.getProject(Long.parseLong(sessionId), projectRepository);
        User user = CommonUtil.getUser();
        if (!userProjectRepository.existsByProjectAndUserAndIsTeam(project, user, true)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        try {
            //녹음 중지
            Recording recording = openVidu.stopRecording(sessionRecordingMap.get(sessionId));
            sessionRecordingMap.remove(sessionId);
            //STT 를 위한 API 전송
            String sttId = sttService.getSttId(recording.getUrl(), true);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            //회의록에 stt api 를 통해 받은 sttId 저장
            Meetingnote meetingNote = Meetingnote.builder()
                    .sttId(sttId)
                    .createdAt(recording.getCreatedAt())
                    .title(format.format(new Date(recording.getCreatedAt())))
                    .duration((long) recording.getDuration())
                    .url(recording.getUrl())
                    .status(false)
                    .project(project)
                    .build();
            Meetingnote savedMeetingnote = meetingNoteRepository.save(meetingNote);
            //stt api를 통해 받은 stt id 로 텍스트 불러오기
            getSttUtterance(sttId, savedMeetingnote);
            return ResponseDto.builder()
                    .ok(true)
                    .message("회의록 저장 성공")
                    .build();

        } catch (OpenViduJavaClientException | OpenViduHttpException exception) {
            sessionRecordingMap.remove(sessionId);
            throw new CustomException(ErrorCode.OPENVIDU_ERROR);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.VITO_H0010);
        }

    }
    //stt api를 통해 받은 stt id 로 텍스트 불러오기
    @Transactional
    public boolean getSttUtterance(String sttId, Meetingnote savedMeetingnote) {
        VitoResponseDto vitoResponseDto = sttService.getSttUtterance(sttId, true);
        if ("completed".equals(vitoResponseDto.getStatus())) {
            //stt 완료
            List<Utterance> utterances = vitoResponseDto.getResults().getUtterances().stream().map(utterance -> Utterance.builder()
                    .start_at(utterance.getStart_at())
                    .duration(utterance.getDuration())
                    .msg(utterance.getMsg())
                    .spk(utterance.getSpk())
                    .meetingnote(savedMeetingnote)
                    .build()).collect(Collectors.toList());
            savedMeetingnote.updateUtterances(utterances);
            return true;
        }
        return false;
    }

    // 회의록 데이터 목록 불러오기
    public List<MeetingNoteDto.Response> getMeetingNotes(Long projectId) {
        Project project = CommonUtil.getProject(projectId, projectRepository);
        User user = CommonUtil.getUser();
        if (!userProjectRepository.existsByProjectAndUserAndIsTeam(project, user, true)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return meetingNoteRepository.findAllByProject(project).stream().map(meetingNote ->
                MeetingNoteDto.Response.builder()
                        .id(meetingNote.getId())
                        .createdAt(meetingNote.getCreatedAt())
                        .title(meetingNote.getTitle())
                        .duration(meetingNote.getDuration())
                        .url(meetingNote.getUrl())
                        .status(meetingNote.getStatus())
                        .build()).collect(Collectors.toList());
    }
    //STT 텍스트 데이터 불러오기
    @Transactional
    public List<UtteranceDto.Response> getMeetingNote(Long meetingNoteId){
        Meetingnote meetingNote = meetingNoteRepository.findById(meetingNoteId).orElseThrow(()-> new CustomException(ErrorCode.NOT_EXIST_MEETING_NOTE));
        User user = CommonUtil.getUser();
        Project project = meetingNote.getProject();
        if (!userProjectRepository.existsByProjectAndUserAndIsTeam(project, user, true)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        if(!meetingNote.getStatus()){
            if(!getSttUtterance(meetingNote.getSttId(), meetingNote)){
                //stt 진행중
                throw new CustomException(ErrorCode.VITO_H0010);
            }
        }
        return utteranceRepository.findAllByMeetingnote(meetingNote).stream().map(utterance ->
                UtteranceDto.Response.builder()
                        .id(utterance.getId())
                        .start_at(utterance.getStart_at())
                        .duration(utterance.getDuration())
                        .msg(utterance.getMsg())
                        .build()
        ).collect(Collectors.toList());
    }
    //openvidu의 recording 목록 불러오기
    public ResponseDto<?> listRecording() throws OpenViduJavaClientException, OpenViduHttpException {

        return ResponseDto.builder().result(openVidu.listRecordings()).build();
    }


    //openvidu 서버에서 토큰을 통해 참여 가능 여부 판별
    public ResponseDto<?> openvidu(String token, Long projectId) {
        User user = CommonUtil.getUserByToken(token, tokenProvider);
        Project project = CommonUtil.getProject(projectId, projectRepository);
        return userProjectRepository.existsByProjectAndUserAndIsTeam(project, user, true) ?
                ResponseDto.builder()
                .ok(true)
                .message("참여 가능한 세션입니다.")
                .build()
                :
                ResponseDto.builder()
                .ok(false)
                .message("참여 불가능한 세션입니다.")
                .build();
    }
}
