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
                    //?????????
                    return ResponseDto.builder()
                            .ok(true)
                            .message("????????? ?????????.")
                            .result(recording)
                            .build();
                } else if (recording.getStatus().equals(Recording.Status.ready)) {
                    //?????? ??????, ???????????? ????????? ??????
                }
                return ResponseDto.builder().message("?????? ????????? ?????????").build();
            }catch (OpenViduJavaClientException | OpenViduHttpException exception){
                // System.out.println(exception.getMessage());
                return ResponseDto.builder()
                        .ok(false)
                        .message("???????????? ?????? ?????? ?????????.")
                        .build();
            }
        }

        //?????? ??????
        Recording recording = null;
        try{
            recording = openVidu.getRecording(sessionId);
        }catch (OpenViduJavaClientException | OpenViduHttpException exception){
            log.info("not current Recording");
        }
        try{
            if(recording!=null && "started".equals(recording.getStatus())){
                //????????????
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
                    .message("?????? ??????")
                    .result(recording)
                    .build();

        }catch (OpenViduJavaClientException | OpenViduHttpException exception){
            sessionRecordingMap.remove(sessionId);
            System.out.println(exception.getMessage());
            throw new CustomException(ErrorCode.OPENVIDU_ERROR);
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
            System.out.println("stop recording");
            Recording recording = openVidu.stopRecording(sessionRecordingMap.get(sessionId));
            sessionRecordingMap.remove(sessionId);
            String sttId = sttService.getSttId(recording.getUrl(), true);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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
            getSttUtterance(sttId, savedMeetingnote);
            return ResponseDto.builder()
                    .ok(true)
                    .message("????????? ?????? ??????")
                    .build();

        } catch (OpenViduJavaClientException | OpenViduHttpException exception) {
            sessionRecordingMap.remove(sessionId);
            System.out.println(exception);
            throw new CustomException(ErrorCode.OPENVIDU_ERROR);
        } catch (IOException e) {
            System.out.println(e);
            throw new CustomException(ErrorCode.VITO_H0010);
        }

    }
    @Transactional
    public boolean getSttUtterance(String sttId, Meetingnote savedMeetingnote) {
        VitoResponseDto vitoResponseDto = sttService.getSttUtterance(sttId, true);
        if ("completed".equals(vitoResponseDto.getStatus())) {
            //stt ??????
            List<Utterance> utterances = vitoResponseDto.getResults().getUtterances().stream().map(utterance -> Utterance.builder()
                    .start_at(utterance.getStart_at())
                    .duration(utterance.getDuration())
                    .msg(utterance.getMsg())
                    .spk(utterance.getSpk())
                    .meetingnote(savedMeetingnote)
                    .build()).collect(Collectors.toList());
            savedMeetingnote.updateUtterances(utterances);
            // meetingNoteRepository.save(meetingNote);
            return true;
        }
        return false;
    }

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
                //stt ?????????
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
    public ResponseDto<?> listRecording() throws OpenViduJavaClientException, OpenViduHttpException {

        return ResponseDto.builder().result(openVidu.listRecordings()).build();
    }
    public ResponseDto<?> testRecording() throws OpenViduJavaClientException, OpenViduHttpException, IOException {

        // String sttId = sttService.getSttId("https://vidutest.shop/openvidu/recordings/ses_LPtXhnb6h8/ses_LPtXhnb6h8.webm", true);
        String sttId = sttService.getSttId("https://hh99.s3.ap-northeast-2.amazonaws.com/1_3.webm", true);
        VitoResponseDto vitoResponseDto = sttService.getSttUtterance(sttId, true);
        return ResponseDto.builder().result(vitoResponseDto).build();
    }

    public ResponseDto<?> openvidu(String token, Long projectId) {
        User user = CommonUtil.getUserByToken(token, tokenProvider);
        Project project = CommonUtil.getProject(projectId, projectRepository);
        return userProjectRepository.existsByProjectAndUserAndIsTeam(project, user, true) ?
                ResponseDto.builder()
                .ok(true)
                .message("?????? ????????? ???????????????.")
                .build()
                :
                ResponseDto.builder()
                .ok(false)
                .message("?????? ???????????? ???????????????.")
                .build();
    }
}
