package com.hanghae.degether.project.service;

import antlr.build.ANTLR;
import com.hanghae.degether.project.dto.ProjectRequestDto;
import com.hanghae.degether.project.dto.ProjectResponseDto;
import com.hanghae.degether.project.exception.ExceptionMessage;
import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.project.model.QProject;
import com.hanghae.degether.project.model.Zzim;
import com.hanghae.degether.project.repository.ProjectQueryDslRepository;
import com.hanghae.degether.project.repository.ProjectRepository;
import com.hanghae.degether.project.repository.ZzimRepository;
import com.hanghae.degether.project.util.CommonUtil;
import com.hanghae.degether.project.util.S3Uploader;
import com.hanghae.degether.user.model.User;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.criterion.Projection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ZzimRepository zzimRepository;
    private final ProjectQueryDslRepository projectQueryDslRepository;
    private final S3Uploader s3Uploader;
    private final String S3Dir = "projectThumbnail";
    @Transactional
    public Long createProject(ProjectRequestDto.Create projectRequestDto, MultipartFile multipartFile) {
        User user = CommonUtil.getUser();
        String thumbnailUrl = "";
        if(!multipartFile.isEmpty()) {
            //이미지 업로드
            thumbnailUrl = s3Uploader.upload(multipartFile, S3Dir);
        }
        //TODO: save try catch 해서 save 오류시 업로드된 이미지 삭제
        return projectRepository.save(Project.builder()
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
                        .language(projectRequestDto.getLanguage())
                        .genre(projectRequestDto.getGenre())
                        .user(user)
                .build()).getId();

    }

    @Transactional(readOnly = true)
    public List<?> getProject(String search, String language, String genre, String step) {
        //TODO: queryDsl 알아보기
        return projectQueryDslRepository.getProjectsBySearch(search, language, genre, step);
        // return projectRepository.findAllBySearchQuery(search, language, genre, step);
    }

    @Transactional
    public void projectZzim(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = projectRepository.findById(projectId).orElseThrow(()-> new IllegalArgumentException(ExceptionMessage.NOT_EXIST_PROJECT));
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

    public ProjectResponseDto.Get modifyProject(Long projectId, ProjectRequestDto.Create projectRequestDto, MultipartFile multipartFile) {
    }
}
