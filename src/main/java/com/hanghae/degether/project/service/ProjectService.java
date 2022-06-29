package com.hanghae.degether.project.service;

import com.hanghae.degether.doc.DocRepository;
import com.hanghae.degether.project.dto.CommentDto;
import com.hanghae.degether.project.dto.DocDto;
import com.hanghae.degether.project.dto.ProjectDto;
import com.hanghae.degether.project.dto.UserDto;
import com.hanghae.degether.project.exception.ExceptionMessage;
import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.project.model.UserProject;
import com.hanghae.degether.project.model.Zzim;
import com.hanghae.degether.project.repository.*;
import com.hanghae.degether.project.util.CommonUtil;
import com.hanghae.degether.project.util.S3Uploader;
import com.hanghae.degether.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    private final ZzimRepository zzimRepository;
    private final DocRepository docRepository;
    private final CommentRepository commentRepository;
    private final ProjectQueryDslRepository projectQueryDslRepository;
    private final S3Uploader s3Uploader;
    private final String S3Dir = "projectThumbnail";
    @Transactional
    public Long createProject(ProjectDto.Request projectRequestDto, MultipartFile multipartFile) {
        User user = CommonUtil.getUser();
        String thumbnailUrl = "";
        if(!multipartFile.isEmpty()) {
            //이미지 업로드
            thumbnailUrl = s3Uploader.upload(multipartFile, S3Dir);
        }
        //save 오류시 업로드된 이미지 삭제
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
                    .languages(projectRequestDto.getLanguage())
                    .genres(projectRequestDto.getGenre())
                    .user(user)
                    .build());
            userProjectRepository.save(UserProject.builder()
                    .project(savedProject)
                    .user(user)
                    .build());
            return savedProject.getId();
        } catch (Exception e) {
            s3Uploader.deleteFromS3(thumbnailUrl);
            throw e;
        }

    }

    @Transactional(readOnly = true)
    public List<?> getProjects(String search, String language, String genre, String step) {
        //TODO: queryDsl 알아보기
        return projectQueryDslRepository.getProjectsBySearch(search, language, genre, step);
        // return projectRepository.findAllBySearchQuery(search, language, genre, step);
    }

    @Transactional
    public void projectZzim(Long projectId) {
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

    @Transactional
    public ProjectDto.Response modifyProject(Long projectId, ProjectDto.Request projectRequestDto, MultipartFile multipartFile) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(ExceptionMessage.UNAUTHORIZED);
        }
        String thumbnail = project.getThumbnail();
        if (!multipartFile.isEmpty()) {
            //프로젝트 수정시 새로운 multipartfile이 오면 이미지 수정
            //기존이미지 삭제
            s3Uploader.deleteFromS3(project.getThumbnail());
            //새로운 이미지 업로드
            thumbnail = s3Uploader.upload(multipartFile, S3Dir);
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
                projectRequestDto.getLanguage(),
                projectRequestDto.getGenre(),
                thumbnail
        );
    }

    public boolean existUser(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        return userProjectRepository.existsByProjectAndUserNot(project, user);
    }


    public void deleteProject(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(ExceptionMessage.UNAUTHORIZED);
        }
        if (!project.getThumbnail().isEmpty() && !"".equals(project.getThumbnail())) {
            //이미지 삭제
            s3Uploader.deleteFromS3(project.getThumbnail());
        }
        projectRepository.delete(project);
    }


    public ProjectDto.Response getProject(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        return ProjectDto.Response.builder()
                .thumbnail(project.getThumbnail())
                .projectName(project.getProjectName())
                .projectDescription(project.getProjectDescription())
                .feCount(project.getFeCount())
                .beCount(project.getBeCount())
                .deCount(project.getDeCount())
                .github(project.getGithub())
                .figma(project.getFigma())
                .deadLine(project.getDeadLine())
                .step(project.getStep())
                .language(project.getLanguages())
                .genre(project.getGenres())
                .comment(
                        project.getComments().stream().map(comment -> CommentDto.Response.builder()
                                .commentId(comment.getId())
                                .nickname(comment.getUser().getNickname())
                                .comment(comment.getComment())
                                .build()).collect(Collectors.toList())
                )
                .build();
    }

    public void applyProject(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (userProjectRepository.existsByProjectAndUser(project, user)) {
            throw new IllegalArgumentException(ExceptionMessage.DUPLICATED_APPLY);
        }
        userProjectRepository.save(UserProject.builder()
                .user(user)
                .project(project)
                .isTeam(false)
                .build());
    }

    public ProjectDto.Response getProjectMain(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!userProjectRepository.existsByProjectAndUserAndTeam(project, user, true)) {
            throw new IllegalArgumentException(ExceptionMessage.UNAUTHORIZED);
        }
        List<UserProject> userProjects = userProjectRepository.findAllByProject(project);
        return ProjectDto.Response.builder()
                .projectName(project.getProjectName())
                .github(project.getGithub())
                .figma(project.getFigma())
                .user(
                        userProjects.stream().filter((UserProject::isTeam))
                                .map((userProjectList) -> {
                                    User user = userProjectList.getUser();
                                    return UserDto.builder()
                                            .userId(user.getId())
                                            .profileUrl(user.getProfileUrl())
                                            .role(user.getRole())
                                            .nickname(user.getNickname())
                                            .build();
                                }).collect(Collectors.toList())
                )
                .applyUser(
                        userProjects.stream().filter((userProject -> !userProject.isTeam()))
                                .map((userProjectList) -> {
                                    User user = userProjectList.getUser();
                                    return UserDto.builder()
                                            .userId(user.getId())
                                            .profileUrl(user.getProfileUrl())
                                            .role(user.getRole())
                                            .nickname(user.getNickname())
                                            .build();
                                }).collect(Collectors.toList())
                )
                .notice(
                        docRepository.findAllByProjectAndNoticeOrderByCreatedDateDesc(project, true).stream().map((doc)->{
                            return DocDto.builder()
                                    .docId(doc.getId())
                                    .title(doc.getTitle())
                                    .nickname(doc.getUser().getNickname())
                                    .createdDate(doc.getCreatedDate())
                                    .build();
                        }).collect(Collectors.toList())
                )
                .todo(
                        docRepository.findAllByProjectAndOnGoingOrderByCreatedDateDesc(project, true).stream().map((doc)->{
                            return DocDto.builder()
                                    .docId(doc.getId())
                                    .title(doc.getTitle())
                                    .inCharge(doc.getInCharge().getNickname())
                                    .createdDate(doc.getCreatedDate())
                                    .docStatus(doc.getDocStatus())
                                    .startDate(doc.getStartDate())
                                    .endDate(doc.getEndDate())
                                    .build();
                        }).collect(Collectors.toList())
                )
                .build();
    }
    @Transactional
    public void addUser(Long projectId, Long userId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(ExceptionMessage.UNAUTHORIZED);
        }
        UserProject userProject = userProjectRepository.findByProjectAndUserId(project, userId).orElseThrow(()->{
            throw new IllegalArgumentException(ExceptionMessage.NOT_APPLY);
        });
        if (userProject.isTeam()) {
            throw new IllegalArgumentException(ExceptionMessage.DUPLICATED_JOIN);
        }
        userProject.changeIsTeam(true);
    }

    public void kickUser(Long projectId, Long userId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(ExceptionMessage.UNAUTHORIZED);
        }
        UserProject userProject = userProjectRepository.findByProjectAndUserId(project, userId).orElseThrow(()->{
            throw new IllegalArgumentException(ExceptionMessage.NOT_APPLY);
        });
        userProjectRepository.delete(userProject);
    }
}
