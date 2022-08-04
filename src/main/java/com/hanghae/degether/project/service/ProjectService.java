package com.hanghae.degether.project.service;

import com.hanghae.degether.doc.repository.DocRepository;
import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
import com.hanghae.degether.project.dto.CommentDto;
import com.hanghae.degether.project.dto.DocDto;
import com.hanghae.degether.project.dto.ProjectDto;
import com.hanghae.degether.project.dto.UserDto;
import com.hanghae.degether.project.model.*;
import com.hanghae.degether.project.repository.*;
import com.hanghae.degether.project.util.CommonUtil;
import com.hanghae.degether.project.util.S3Uploader;
import com.hanghae.degether.sse.NotificationService;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    private final ZzimRepository zzimRepository;
    private final DocRepository docRepository;
    private final ProjectQueryDslRepository projectQueryDslRepository;
    private final S3Uploader s3Uploader;
    private final String S3ThumbnailDir = "projectThumbnail";
    private final String S3InfoFileDir = "projectInfo";
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final NotificationService notificationService;
    // http, https 가 없을때 https 추가
    public String addHtmlPrefix(String url){
        if(!"".equals(url) && url!=null){
            // null이 아닌 url
            if (!(url.startsWith("http://") || url.startsWith("https://"))) {
                // http, https 가 없을때 https 추가
                return "https://" + url;
            }
        }
        return url;
    }
    //프로젝트 생성
    @Transactional
    public Long createProject(ProjectDto.Request projectRequestDto, MultipartFile multipartFile, List<MultipartFile> infoFiles) {
        User user = CommonUtil.getUser();



        // 유지보수 중이 아닌 프로젝트가 3개 이상일때 생성 불가능
        if (userProjectRepository.countByUserAndIsTeamAndProject_StepNot(user, true, "유지보수") > 2) {

            throw new CustomException(ErrorCode.MANY_PROJECT);
        }

        String thumbnailUrl = "";
        List<String> infoFileUrls = new ArrayList<>();
        if(multipartFile != null) {
            //이미지 업로드
            thumbnailUrl = s3Uploader.upload(multipartFile, S3ThumbnailDir);
        }
        if (infoFiles != null) {
            for (MultipartFile infoFile : infoFiles) {
                //프로젝트 정보 파일 업로드
                String infoFileUrl = s3Uploader.upload(infoFile, S3InfoFileDir);
                infoFileUrls.add(infoFileUrl);
            }
        }

        // http, https 가 없을때 https 추가
        projectRequestDto.setGithub(addHtmlPrefix(projectRequestDto.getGithub()));
        projectRequestDto.setFigma(addHtmlPrefix(projectRequestDto.getFigma()));
        try {
            Project savedProject = projectRepository.save(Project.builder()
                    .thumbnail(thumbnailUrl)
                    .projectName(projectRequestDto.getProjectName())
                    .projectDescription(projectRequestDto.getProjectDescription())
                    .feCount(projectRequestDto.getFeCount())
                    .beCount(projectRequestDto.getBeCount())
                    .deCount(projectRequestDto.getDeCount())
                    .github(projectRequestDto.getGithub())
                    .figma(projectRequestDto.getFigma())
                    .deadLine(projectRequestDto.getDeadLine())
                    .step(projectRequestDto.getStep())
                    .languages(projectRequestDto.getLanguage().stream().map((string) -> Language.builder().language(string).build()).collect(Collectors.toList()))
                    .genres(projectRequestDto.getGenre().stream().map((string) -> Genre.builder().genre(string).build()).collect(Collectors.toList()))
                    .user(user)
                    .infoFiles(infoFileUrls)
                    .build());

            userProjectRepository.save(UserProject.builder()
                    .project(savedProject)
                    .isTeam(true)
                    .user(user)
                    .build());

            return savedProject.getId();
        } catch (Exception e) {
            log.info("delete Img");
            //save 오류시 업로드된 이미지 삭제
            s3Uploader.deleteFromS3(s3Uploader.getFileName(thumbnailUrl));
            for (String infoFileUrl : infoFileUrls) {
                s3Uploader.deleteFromS3(s3Uploader.getFileName(infoFileUrl));
            }
            throw e;
        }

    }
    //프로젝트 목록
    @Transactional(readOnly = true)
    public ProjectDto.Slice getProjects(String search, String language, String genre, String step, String token, int page, String sorted) {
        //유저 유효성 판별
        User user = CommonUtil.getUserByToken(token, jwtTokenProvider);
        Sort.Direction direction = Sort.Direction.DESC;
        if("deadLine".equals(sorted)) {
            direction = Sort.Direction.ASC;
        }
        Sort sort = Sort.by(direction, sorted);
        Pageable pageable = PageRequest.of(page, 18, sort);
        Slice<Project> slice = projectQueryDslRepository.getProjectsBySearch(search, language, genre, step, pageable);
        List<ProjectDto.Response> content = slice.getContent().stream().map(project -> {
            boolean isZzim;

            if(user==null) isZzim = false;
            else {
                //로그인한 상태일 시 해당 프로젝트에 찜 여부 판별
                isZzim = zzimRepository.existsByProjectAndUser(project,user);
            }
            int devCount = project.getBeCount() + project.getFeCount();
            int deCount = project.getDeCount();
            for (UserProject userProject : project.getUserProjects()) {
                String role = userProject.getUser().getRole();
                if ("백엔드 개발자".equals(role) || "프론트엔드 개발자".equals(role)) {
                    //참여가능한 개발자 인원수
                    devCount--;
                } else if ("디자이너".equals(role)) {
                    //참여가능한 디자이너 인원수
                    deCount--;
                }
            }
            return ProjectDto.Response.builder()
                    .projectId(project.getId())
                    .thumbnail(project.getThumbnail())
                    .projectName(project.getProjectName())
                    .projectDescription(project.getProjectDescription())
                    .devCount(devCount)
                    .deCount(deCount)
                    .github(project.getGithub())
                    .figma(project.getFigma())
                    .deadLine(project.getDeadLine())
                    .dDay(
                            Duration.between(LocalDate.now().atStartOfDay(),project.getDeadLine().atStartOfDay()).toDays()
                    )
                    .step(project.getStep())
                    .language(project.getLanguages().stream().map(Language::getLanguage).collect(Collectors.toList()))
                    //프론트의 요청으로 language를 string 배열이 아닌 ,를 구분자로 갖는 string으로 변환
                    .languageString(project.getLanguages().stream().map(Language::getLanguage).collect(Collectors.joining(", ")))
                    .genre(project.getGenres().stream().map(Genre::getGenre).collect(Collectors.toList()))
                    .step(project.getStep())
                    .isZzim(isZzim)
                    .zzimCount(zzimRepository.countByProject(project))
                    .build();
        }).collect(Collectors.toList());
        return ProjectDto.Slice.builder()
                .isLast(!slice.hasNext())
                .list(content)
                .build();
    }
    //마이 프로젝트 목록
    public List<ProjectDto.Response> getMyProjects() {
        //유저 판별
        User user = CommonUtil.getUser();

        return userProjectRepository.findAllByIsTeamAndUser(true,user).stream().map(
                userProject -> {
                    //Todo: 쿼리 개선 필요
                    Project project = userProject.getProject();
                    int devCount = 0;
                    int deCount = 0;
                    for (UserProject userProject2 : project.getUserProjects()) {
                        String role = userProject2.getUser().getRole();
                        if ("백엔드 개발자".equals(role) || "프론트엔드 개발자".equals(role)) {
                            //참여가능한 개발자 인원수
                            devCount++;
                        } else if ("디자이너".equals(role)) {
                            //참여가능한 디자이너 인원수
                            deCount++;
                        }
                    }
                    return ProjectDto.Response.builder()
                            .projectId(project.getId())
                            .projectName(project.getProjectName())
                            .devCount(devCount)
                            .deCount(deCount)
                            .thumbnail(project.getThumbnail())
                            .build();
                }).collect(Collectors.toList());

    }
    //프로젝트 찜하기
    @Transactional
    public void projectZzim(Long projectId) {
        //유저, 프로젝트 판별
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (zzimRepository.existsByUserAndProject(user, project)) {
            //찜삭제
            zzimRepository.deleteByUserAndProject(user, project);
        }else {
            //찜하기
            zzimRepository.save(Zzim.builder()
                    .user(user)
                    .project(project)
                    .build());
        }

    }
    //프로젝트 수정
    @Transactional
    public ProjectDto.Response modifyProject(Long projectId, ProjectDto.Request projectRequestDto, MultipartFile multipartFile) {
        //프로젝트, 유저 유효성 판별
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }


        String thumbnail = project.getThumbnail();
        if (multipartFile != null) {
            //프로젝트 수정시 새로운 multipartfile이 오면 이미지 수정
            //기존이미지 삭제
            if(project.getThumbnail()!=null && !"".equals(project.getThumbnail())){
                s3Uploader.deleteFromS3(s3Uploader.getFileName(project.getThumbnail()));
            }
            //새로운 이미지 업로드
            thumbnail = s3Uploader.upload(multipartFile, S3ThumbnailDir);
        }

        // http, https 가 없을때 https 추가
        projectRequestDto.setGithub(addHtmlPrefix(projectRequestDto.getGithub()));
        projectRequestDto.setFigma(addHtmlPrefix(projectRequestDto.getFigma()));
        return project.update(
                projectRequestDto.getProjectName(),
                projectRequestDto.getProjectDescription(),
                projectRequestDto.getFeCount(),
                projectRequestDto.getBeCount(),
                projectRequestDto.getDeCount(),
                projectRequestDto.getGithub(),
                projectRequestDto.getFigma(),
                projectRequestDto.getDeadLine(),
                projectRequestDto.getStep(),
                projectRequestDto.getLanguage().stream().map((string)-> Language.builder().language(string).build()).collect(Collectors.toList()),
                projectRequestDto.getGenre().stream().map((string)-> Genre.builder().genre(string).build()).collect(Collectors.toList()),
                thumbnail
        );
    }
    //프로젝트 정보 파일 수정
    @Transactional
    public String modifyInfoFile(Long projectId, String fileUrl, MultipartFile infoFile) {
        //프로젝트, 유저 유효성 판별
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        List<String> infoFiles = project.getInfoFiles();
        String infoFileUrl = null;
        //파일 삭제
        if (fileUrl != null) {
            //파일 삭제, 수정
            infoFiles.remove(fileUrl);
            s3Uploader.deleteFromS3(s3Uploader.getFileName(fileUrl));
        }
        if (infoFile != null) {
            //파일 추가, 수정
            infoFileUrl = s3Uploader.upload(infoFile,S3InfoFileDir);
            infoFiles.add(infoFileUrl);
        }
        project.infoFilesUpdate(infoFiles);
        return infoFileUrl;
    }
    //프로젝트에 참여중인 유저가 존재하는지 판별
    public boolean existUser(Long projectId) {
        //프로젝트, 유저 유효성 판별
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        return userProjectRepository.existsByProjectAndUserNot(project, user);
    }

    //프로젝트 삭제
    @Transactional
    public void deleteProject(Long projectId) {
        //프로젝트, 유저 유효성 판별
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        if (project.getThumbnail() != null && !"".equals(project.getThumbnail())) {
            // 이미지 삭제
            s3Uploader.deleteFromS3(s3Uploader.getFileName(project.getThumbnail()));
        }
        for (String infoFileUrl : project.getInfoFiles()) {
            // 프로젝트 정보파일삭제
            s3Uploader.deleteFromS3(s3Uploader.getFileName(infoFileUrl));
        }
        zzimRepository.deleteByProject(project);
        projectRepository.delete(project);
    }

    //프로젝트 상세
    public ProjectDto.Response getProject(Long projectId) {
        // 프로젝트 유효성 판별
        Project project = CommonUtil.getProject(projectId, projectRepository);

        return ProjectDto.Response.builder()
                .thumbnail(project.getThumbnail())
                .projectName(project.getProjectName())
                .projectDescription(project.getProjectDescription())
                .feCurrentCount((int) project.getUserProjects().stream().filter(userProject -> Objects.equals("프론트 개발자", userProject.getUser().getRole())).count())
                .beCurrentCount((int) project.getUserProjects().stream().filter(userProject -> Objects.equals("백엔드 개발자", userProject.getUser().getRole())).count())
                .deCurrentCount((int) project.getUserProjects().stream().filter(userProject -> Objects.equals("디자이너", userProject.getUser().getRole())).count())
                .feCount(project.getFeCount())
                .beCount(project.getBeCount())
                .deCount(project.getDeCount())
                .github(project.getGithub())
                .figma(project.getFigma())
                .deadLine(project.getDeadLine())
                .step(project.getStep())
                .language(project.getLanguages().stream().map(Language::getLanguage).collect(Collectors.toList()))
                .languageString(project.getLanguages().stream().map(Language::getLanguage).collect(Collectors.joining(", ")))
                .genre(project.getGenres().stream().map(Genre::getGenre).collect(Collectors.toList()))
                .comment(
                        project.getComments().stream().map(comment -> CommentDto.Response.builder()
                                .commentId(comment.getId())
                                .nickname(comment.getUser().getNickname())
                                .comment(comment.getComment())
                                .build()).collect(Collectors.toList())
                )
                .infoFiles(project.getInfoFiles().stream().map(s ->
                        ProjectDto.File.builder()
                        .fileUrl(s)
                        .fileName(s3Uploader.getOriginalFileName(s,S3InfoFileDir))
                        .build())
                        .collect(Collectors.toList()))

                .build();
    }
    //프로젝트 지원
    public void applyProject(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (userProjectRepository.existsByProjectAndUser(project, user)) {
            throw new CustomException(ErrorCode.DUPLICATED_APPLY);
        }
        if(userProjectRepository.countByUserAndIsTeamAndProject_StepNot(user, true,"유지보수") > 2){
            //지원 불가
            throw new CustomException(ErrorCode.MANY_PROJECT);
        }
        userProjectRepository.save(UserProject.builder()
                .user(user)
                .project(project)
                .isTeam(false)
                .build());
        //sse 이벤트 전송
        notificationService.save(project.getUser(),"프로젝트 "+project.getProjectName()+"에 "+user.getNickname()+" 님이 지원하였습니다.");
    }
    //프로젝트 메인페이지 정보
    public ProjectDto.Response getProjectMain(Long projectId) {
        //프로젝트, 유저 유효성 판별
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!userProjectRepository.existsByProjectAndUserAndIsTeam(project, user, true)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return ProjectDto.Response.builder()
                .projectName(project.getProjectName())
                .feCount(project.getFeCount())
                .beCount(project.getBeCount())
                .deCount(project.getDeCount())
                .github(project.getGithub())
                .dDay(
                        Duration.between(LocalDate.now().atStartOfDay(),project.getDeadLine().atStartOfDay()).toDays()
                )
                .figma(project.getFigma())
                .leaderId(project.getUser().getId())
                .user(
                        project.getUserProjects().stream().filter((UserProject::isTeam))
                                .map((userProjectList) -> {
                                    User userStream = userProjectList.getUser();
                                    return UserDto.builder()
                                            .userId(userStream.getId())
                                            .profileUrl(userStream.getProfileUrl())
                                            .role(userStream.getRole())
                                            .nickname(userStream.getNickname())
                                            .build();
                                }).collect(Collectors.toList())
                )
                .applyUser(
                        project.getUserProjects().stream().filter((userProject -> !userProject.isTeam()))
                                .map((userProjectList) -> {
                                    User userStream = userProjectList.getUser();
                                    return UserDto.builder()
                                            .userId(userStream.getId())
                                            .profileUrl(userStream.getProfileUrl())
                                            .role(userStream.getRole())
                                            .nickname(userStream.getNickname())
                                            .build();
                                }).collect(Collectors.toList())
                )
                .notice(
                        docRepository.findAllByProjectAndNoticeOrderByCreatedDateDesc(project, true).stream().map((doc)-> DocDto.builder()
                                .docId(doc.getId())
                                .title(doc.getTitle())
                                .nickname(doc.getUser().getNickname())
                                .createdDate(doc.getCreatedDate())
                                .build()).collect(Collectors.toList())
                )
                .todo(
                        docRepository.findAllByProjectAndOnGoingOrderByCreatedDateDesc(project, true).stream().map((doc)-> DocDto.builder()
                                .docId(doc.getId())
                                .title(doc.getTitle())
                                .inCharge(doc.getInCharge().getNickname())
                                .createdDate(doc.getCreatedDate())
                                .docStatus(doc.getDocStatus())
                                .startDate(doc.getStartDate())
                                .endDate(doc.getEndDate())
                                .dDay(
                                        Duration.between(LocalDate.now().atStartOfDay(),doc.getEndDate().atStartOfDay()).toDays()
                                )
                                .build()).collect(Collectors.toList())
                )
                .build();
    }
    //유저 초대하기
    @Transactional
    public void addUser(Long projectId, Long userId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        User userSearch = userRepository.findById(userId).orElseThrow(()->
                new CustomException(ErrorCode.NOT_EXIST_USER)
        );
        UserProject userProject = userProjectRepository.findByProjectAndUser(project, userSearch).orElseThrow(()->
                new CustomException(ErrorCode.NOT_APPLY)
        );
        if (userProject.isTeam()) {
            throw new CustomException(ErrorCode.DUPLICATED_JOIN);
        }

        if(userProjectRepository.countByUserAndIsTeamAndProject_StepNot(userSearch, true,"유지보수") > 2){
            //초대 불가
            throw new CustomException(ErrorCode.MANY_PROJECT);
        }
        userProject.changeIsTeam(true);
        //sse 이벤트 전송
        notificationService.save(userSearch,"프로젝트 "+project.getProjectName()+"에 지원 요청이 승낙되었습니다.");

    }
    //유저 프로젝트 탈퇴
    public void kickUser(Long projectId, Long userId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId()) && !userId.equals(user.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        User userSearch = userRepository.findById(userId).orElseThrow(()->
                new CustomException(ErrorCode.NOT_EXIST_USER)
        );
        UserProject userProject = userProjectRepository.findByProjectAndUser(project, userSearch).orElseThrow(()->
            new CustomException(ErrorCode.NOT_APPLY)
        );
        if(!userProject.isTeam()){
            // 지원 거절
            //sse 이벤트 전송
            notificationService.save(userSearch,"프로젝트 "+project.getProjectName()+"에 지원 요청이 거절되었습니다.");
        }
        userProjectRepository.delete(userProject);
    }

}
