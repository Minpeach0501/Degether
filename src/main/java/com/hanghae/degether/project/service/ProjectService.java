package com.hanghae.degether.project.service;

import com.hanghae.degether.doc.repository.DocRepository;
import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
import com.hanghae.degether.project.dto.CommentDto;
import com.hanghae.degether.project.dto.DocDto;
import com.hanghae.degether.project.dto.ProjectDto;
import com.hanghae.degether.project.dto.UserDto;
import com.hanghae.degether.project.model.*;
import com.hanghae.degether.project.repository.ProjectQueryDslRepository;
import com.hanghae.degether.project.repository.ProjectRepository;
import com.hanghae.degether.project.repository.UserProjectRepository;
import com.hanghae.degether.project.repository.ZzimRepository;
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


    @Transactional
    public Long createProject(ProjectDto.Request projectRequestDto, MultipartFile multipartFile, List<MultipartFile> infoFiles) {
        User user = CommonUtil.getUser();

        //???????????? ?????? ?????? ??????????????? 3??? ???????????? ?????? ?????????
        // if(projectRepository.countByUserAndStepIsNot(user, "????????????")>=3){
        //     throw new CustomException(ErrorCode.MANY_PROJECT);
        // }

        String thumbnailUrl = "";
        List<String> infoFileUrls = new ArrayList<>();
        if(multipartFile != null) {
            //????????? ?????????
            thumbnailUrl = s3Uploader.upload(multipartFile, S3ThumbnailDir);
        }
        if (infoFiles != null) {
            for (MultipartFile infoFile : infoFiles) {
                String infoFileUrl = s3Uploader.upload(infoFile, S3InfoFileDir);
                infoFileUrls.add(infoFileUrl);
            }
        }
        //save ????????? ???????????? ????????? ??????
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
            s3Uploader.deleteFromS3(s3Uploader.getFileName(thumbnailUrl));
            for (String infoFileUrl : infoFileUrls) {
                s3Uploader.deleteFromS3(s3Uploader.getFileName(infoFileUrl));
            }
            throw e;
        }

    }

    @Transactional(readOnly = true)
    public ProjectDto.Slice getProjects(String search, String language, String genre, String step, String token, int page, String sorted) {
        User user = CommonUtil.getUserByToken(token, jwtTokenProvider);
        // List<Project> list = projectQueryDslRepository.getProjectsBySearch(search, language, genre, step);
        // List<Project> list = projectRepository.findAllByProjectNameContainsAndLanguages_LanguageAndGenres_GenreAndStep("????????????", "spring", "???", "??????");
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
                isZzim = zzimRepository.existsByProjectAndUser(project,user);
            }
            int devCount = project.getBeCount() + project.getFeCount();
            int deCount = project.getDeCount();
            for (UserProject userProject : project.getUserProjects()) {
                String role = userProject.getUser().getRole();
                if ("????????? ?????????".equals(role) || "??????????????? ?????????".equals(role)) {
                    devCount--;
                } else if ("????????????".equals(role)) {
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

    public List<ProjectDto.Response> getMyProjects() {
        User user = CommonUtil.getUser();

        return userProjectRepository.findAllByIsTeamAndUser(true,user).stream().map(
                userProject -> {
                    //Todo: ?????? ?????? ??????
                    Project project = userProject.getProject();
                    int devCount = 0;
                    int deCount = 0;
                    for (UserProject userProject2 : project.getUserProjects()) {
                        String role = userProject2.getUser().getRole();
                        if ("????????? ?????????".equals(role) || "??????????????? ?????????".equals(role)) {
                            devCount++;
                        } else if ("????????????".equals(role)) {
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
    @Transactional
    public void projectZzim(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (zzimRepository.existsByUserAndProject(user, project)) {
            //?????????
            zzimRepository.deleteByUserAndProject(user, project);
        }else {
            //?????????
            zzimRepository.save(Zzim.builder()
                    .user(user)
                    .project(project)
                    .build());
        }

    }

    @Transactional
    public ProjectDto.Response modifyProject(Long projectId, ProjectDto.Request projectRequestDto, MultipartFile multipartFile) {
        //TODO: language update??? ?????? ????????? ?????? ??????
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        String thumbnail = project.getThumbnail();
        if (multipartFile != null) {
            //???????????? ????????? ????????? multipartfile??? ?????? ????????? ??????
            //??????????????? ??????
            s3Uploader.deleteFromS3(s3Uploader.getFileName(project.getThumbnail()));
            //????????? ????????? ?????????
            thumbnail = s3Uploader.upload(multipartFile, S3ThumbnailDir);
        }

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

    @Transactional
    public String modifyInfoFile(Long projectId, String fileUrl, MultipartFile infoFile) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        List<String> infoFiles = project.getInfoFiles();
        String infoFileUrl = null;
        //?????? ??????
        if (fileUrl != null) {
            //?????? ??????, ??????
            infoFiles.remove(fileUrl);
            s3Uploader.deleteFromS3(s3Uploader.getFileName(fileUrl));
        }
        if (infoFile != null) {
            //?????? ??????, ??????
            infoFileUrl = s3Uploader.upload(infoFile,S3InfoFileDir);
            infoFiles.add(infoFileUrl);
        }
        project.infoFilesUpdate(infoFiles);
        return infoFileUrl;
    }

    public boolean existUser(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        return userProjectRepository.existsByProjectAndUserNot(project, user);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        if (project.getThumbnail() != null && !"".equals(project.getThumbnail())) {
            // ????????? ??????
            s3Uploader.deleteFromS3(s3Uploader.getFileName(project.getThumbnail()));
        }
        for (String infoFileUrl : project.getInfoFiles()) {
            s3Uploader.deleteFromS3(s3Uploader.getFileName(infoFileUrl));
        }
        zzimRepository.deleteByProject(project);
        projectRepository.delete(project);
    }


    public ProjectDto.Response getProject(Long projectId) {
        // User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        System.out.println(project.getComments());
        return ProjectDto.Response.builder()
                .thumbnail(project.getThumbnail())
                .projectName(project.getProjectName())
                .projectDescription(project.getProjectDescription())
                .feCurrentCount((int) project.getUserProjects().stream().filter(userProject -> Objects.equals("front", userProject.getUser().getRole())).count())
                .beCurrentCount((int) project.getUserProjects().stream().filter(userProject -> Objects.equals("back", userProject.getUser().getRole())).count())
                .deCurrentCount((int) project.getUserProjects().stream().filter(userProject -> Objects.equals("designer", userProject.getUser().getRole())).count())
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

    public void applyProject(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (userProjectRepository.existsByProjectAndUser(project, user)) {
            throw new CustomException(ErrorCode.DUPLICATED_APPLY);
        }
        userProjectRepository.save(UserProject.builder()
                .user(user)
                .project(project)
                .isTeam(false)
                .build());
        notificationService.save(project.getUser(),"???????????? "+project.getProjectName()+"??? "+user.getNickname()+" ?????? ?????????????????????.");
    }

    public ProjectDto.Response getProjectMain(Long projectId) {
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
        userProject.changeIsTeam(true);
        notificationService.save(userSearch,"???????????? "+project.getProjectName()+"??? ?????? ????????? ?????????????????????.");

    }

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
            // ?????? ??????
            notificationService.save(userSearch,"???????????? "+project.getProjectName()+"??? ?????? ????????? ?????????????????????.");
        }
        userProjectRepository.delete(userProject);
    }

}
