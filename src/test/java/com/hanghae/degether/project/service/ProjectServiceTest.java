package com.hanghae.degether.project.service;

import com.hanghae.degether.project.dto.ProjectDto;
import com.hanghae.degether.project.model.Genre;
import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.project.model.UserProject;
import com.hanghae.degether.project.repository.ProjectRepository;

import com.hanghae.degether.project.repository.UserProjectRepository;
import com.hanghae.degether.project.util.CommonUtil;
import com.hanghae.degether.project.util.S3Uploader;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @MockBean
    S3Uploader s3Uploader;

    @Autowired
    ProjectService projectService;
    @Autowired
    UserRepository userRepository;
    public static MockedStatic<CommonUtil> mCommonUtil;
    @BeforeAll
    public static void beforeAllStatic(){
        System.out.println("static");
        mCommonUtil = Mockito.mockStatic(CommonUtil.class);
    }

    @AfterAll
    public static void afterAll() {
        mCommonUtil.close();
    }
    @BeforeEach
    void beforeEach(){
        mCommonUtil.when(()->CommonUtil.getUser()).thenReturn(
                userRepository.save(
                        User.builder()
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
                )
        );
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
        MockMultipartFile multipartFile = new MockMultipartFile("data", "multipartFile", "img", "multipartFile".getBytes());
        MockMultipartFile infoFile1 = new MockMultipartFile("data", "infoFile1", "img", "infoFile1".getBytes());
        MockMultipartFile infoFile2 = new MockMultipartFile("data", "infoFile2", "img", "infoFile2".getBytes());
        List<MultipartFile> infoFiles = Arrays.asList(infoFile1, infoFile2);
        Mockito.when(s3Uploader.upload(multipartFile, "projectThumbnail")).thenReturn("thumbnailUrl");
        Mockito.when(s3Uploader.upload(infoFile1, "projectInfo")).thenReturn("infoFileUrl1");
        Mockito.when(s3Uploader.upload(infoFile2, "projectInfo")).thenReturn("infoFileUrl2");

        //when
        Long savedProjectId = projectService.createProject(requestDto,multipartFile, infoFiles);
        System.out.println(savedProjectId);
        //then
        Assertions.assertTrue(savedProjectId > 0L);
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