package com.hanghae.degether.project.service;

import com.hanghae.degether.project.dto.ProjectDto;
import com.hanghae.degether.project.model.Genre;
import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.project.repository.ProjectRepository;

import com.hanghae.degether.project.util.CommonUtil;
import com.hanghae.degether.project.util.S3Uploader;
import com.hanghae.degether.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Nested;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class ProjectServiceTest {
    @Mock
    ProjectRepository projectRepository;
    @Mock
    S3Uploader s3Uploader;
    @Autowired
    ProjectService projectService;

    public Project dtoToEntity(ProjectDto.Request dto){
        return Project.builder()
                .projectName(dto.getProjectName())
                .
    }

    @DisplayName("프로젝트 생성")
    @Test
    void createProject() {
        //given
        ProjectDto.Request requestDto = ProjectDto.Request.builder()
                .projectName("projectName")
                .projectDescription("projectDescription")
                .feCount(2)
                .beCount(2)
                .deCount(2)
                .github("http://github.com")
                .figma("http://figma.com")
                .deadLine(LocalDate.of(2002, 7, 31))
                .step("기획")
                .language(Arrays.asList("java", "python"))
                .genre(Arrays.asList("java", "python"))
                .build();
        MockMultipartFile multipartFile = new MockMultipartFile("data", "filename.csv", "img", "multipartFile".getBytes());
        Mockito.when(CommonUtil.getUser()).thenReturn(
                User.builder()
                        .Id(1L)
                        .username("username")
                        .nickname("nickname")
                        .password("password")
                        .language(Arrays.asList(
                                Language.builder().language("java").build(),
                                Language.builder().language("python").build()
                        ))
                        .profileUrl("profileUrl")
                        .role("백엔드 개발자")
                        .github("github.com")
                        .figma("figma.com")
                        .intro("안녕하세요")
                        .email("test@test.com")
                        .phoneNumber("01011112222")
                        .status(true)
                        .build()
                );
        Mockito.when(s3Uploader.upload(multipartFile, "projectThumbnail")).thenReturn("thumbnailUrl");
        String thumbnailUrl = "thumbnailUrl";
        List<String> infoFileUrls = Arrays.asList("infoFileUrl1", "infoFileUrl2");
        Mockito.when(projectRepository.save(
                Project.builder()
                        // .thumbnail(thumbnailUrl)
                        // .projectName(requestDto.getProjectName())
                        // .projectDescription(requestDto.getProjectDescription())
                        // .feCount(requestDto.getFeCount())
                        // .beCount(requestDto.getBeCount())
                        // .deCount(requestDto.getDeCount())
                        // .github(requestDto.getGithub())
                        // .figma(requestDto.getFigma())
                        // .deadLine(requestDto.getDeadLine())
                        // .step(requestDto.getStep())
                        // .languages(requestDto.getLanguage().stream().map((string) -> Language.builder().language(string).build()).collect(Collectors.toList()))
                        // .genres(requestDto.getGenre().stream().map((string) -> Genre.builder().genre(string).build()).collect(Collectors.toList()))
                        // .user(user)
                        // .infoFiles(infoFileUrls)
                        .build()
        )).thenReturn(
                Project.builder()
                        .thumbnail(thumbnailUrl)
                        .projectName(requestDto.getProjectName())
                        .projectDescription(requestDto.getProjectDescription())
                        .feCount(requestDto.getFeCount())
                        .beCount(requestDto.getBeCount())
                        .deCount(requestDto.getDeCount())
                        .github(requestDto.getGithub())
                        .figma(requestDto.getFigma())
                        .deadLine(requestDto.getDeadLine())
                        .step(requestDto.getStep())
                        .languages(requestDto.getLanguage().stream().map((string) -> Language.builder().language(string).build()).collect(Collectors.toList()))
                        .genres(requestDto.getGenre().stream().map((string) -> Genre.builder().genre(string).build()).collect(Collectors.toList()))
                        .user(User.builder()
                                .Id(1L)
                                .username("username")
                                .nickname("nickname")
                                .password("password")
                                .language(Arrays.asList(
                                        Language.builder().language("java").build(),
                                        Language.builder().language("python").build()
                                ))
                                .profileUrl("profileUrl")
                                .role("백엔드 개발자")
                                .github("github.com")
                                .figma("figma.com")
                                .intro("안녕하세요")
                                .email("test@test.com")
                                .phoneNumber("01011112222")
                                .status(true)
                                .build())
                        .infoFiles(infoFileUrls)
                        .build()
        );
        //when
        //then
    }

    @Test
    void getProjects() {
    }

    @Test
    void getMyProjects() {
    }

    @Test
    void projectZzim() {
    }

    @Test
    void modifyProject() {
    }

    @Test
    void modifyInfoFile() {
    }

    @Test
    void existUser() {
    }

    @Test
    void deleteProject() {
    }

    @Test
    void getProject() {
    }

    @Test
    void applyProject() {
    }

    @Test
    void getProjectMain() {
    }

    @Test
    void addUser() {
    }

    @Test
    void kickUser() {
    }
}