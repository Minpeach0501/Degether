//package com.hanghae.degether.project.service;
//
//import com.hanghae.degether.project.dto.ProjectDto;
//import com.hanghae.degether.project.model.Genre;
//import com.hanghae.degether.project.model.Language;
//import com.hanghae.degether.project.model.Project;
//import com.hanghae.degether.project.model.UserProject;
//import com.hanghae.degether.project.repository.ProjectRepository;
//import com.hanghae.degether.project.repository.UserProjectRepository;
//import com.hanghae.degether.project.repository.ZzimRepository;
//import com.hanghae.degether.project.util.S3Uploader;
//import com.hanghae.degether.user.model.User;
//import com.hanghae.degether.user.repository.UserRepository;
//import com.hanghae.degether.user.security.JwtTokenProvider;
//import org.junit.jupiter.api.*;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
//@Transactional
//public class ProjectServiceTest {
//
//    @MockBean
//    S3Uploader s3Uploader;
//
//    @Autowired
//    ProjectService projectService;
//    @Autowired
//    UserRepository userRepository;
//    @Autowired
//    ProjectRepository projectRepository;
//    @Autowired
//    UserProjectRepository userProjectRepository;
//    @Autowired
//    ZzimRepository zzimRepository;
//    @Autowired
//    JwtTokenProvider jwtTokenProvider;
//
//
//    User user;
//    User applyUser;
//
//    String token;
//    Project project;
//    @BeforeAll
//    static void beforeAll(){
//    }
//    @BeforeEach
//    void beforeEach(){
//        user = userRepository.save(
//                User.builder()
//                        .username("username")
//                        .nickname("nickname")
//                        .password("password")
//                        .language(Arrays.asList(
//                                Language.builder().language("java").build(),
//                                Language.builder().language("python").build()
//                        ))
//                        .profileUrl("profileUrl")
//                        .role("????????? ?????????")
//                        .github("github.com")
//                        .figma("figma.com")
//                        .intro("???????????????")
//                        .email("test@test.com")
//                        .phoneNumber("01011112222")
//                        .status(true)
//                        .build()
//        );
//        token = jwtTokenProvider.createToken(user.getUsername());
//        Authentication authentication = jwtTokenProvider.getAuthentication(token);
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        UserProject userProject = UserProject.builder()
//                .isTeam(true)
//                .user(user)
//                .build();
//        project = projectRepository.save(
//                Project.builder()
//                        .thumbnail("thumbnail")
//                        .projectName("projectName")
//                        .projectDescription("projectDescription")
//                        .feCount(2)
//                        .beCount(2)
//                        .deCount(2)
//                        .github("http://github.com")
//                        .figma("http://figma.com")
//                        .deadLine(LocalDate.now().plusDays(1))
//                        .step("??????")
//                        .languages(new ArrayList<>(Arrays.asList(
//                                    Language.builder().language("java").build(),
//                                    Language.builder().language("java").build()
//                                )
//                            )
//                        )
//                        .genres(new ArrayList<>(Arrays.asList(
//                                    Genre.builder().genre("???").build(),
//                                    Genre.builder().genre("??????").build()
//                                )
//                            )
//                        )
//                        // .userProjects(userProjects)
//                        .userProjects(new ArrayList<>(Arrays.asList(userProject)))
//                        .user(user)
//                        .infoFiles(new ArrayList<>(Arrays.asList("infoFile1","infoFile2")))
//                        .comments(Collections.emptyList())
//                        .build()
//        );
//        userProject.updateProject(project);
//    }
//    @AfterEach
//    void afterEach(){
//    }
//
//    @Nested
//    @DisplayName("???????????? ??????")
//    class CreateProject {
//        @DisplayName("??????")
//        @Test
//        void createProject_success() {
//            // given
//            ProjectDto.Request requestDto = ProjectDto.Request.builder()
//                    .projectName("projectName")
//                    .projectDescription("projectDescription")
//                    .feCount(2)
//                    .beCount(2)
//                    .deCount(2)
//                    .github("http://github.com")
//                    .figma("http://figma.com")
//                    .deadLine(LocalDate.now().plusDays(1))
//                    .step("??????")
//                    .language(new ArrayList<>(Arrays.asList("java", "python")))
//                    .genre(new ArrayList<>(Arrays.asList("???", "??????")))
//                    .build();
//            MockMultipartFile multipartFile = new MockMultipartFile("data", "multipartFile", "img", "multipartFile".getBytes());
//            MockMultipartFile infoFile1 = new MockMultipartFile("data", "infoFile1", "img", "infoFile1".getBytes());
//            MockMultipartFile infoFile2 = new MockMultipartFile("data", "infoFile2", "img", "infoFile2".getBytes());
//            List<MultipartFile> infoFiles = Arrays.asList(infoFile1, infoFile2);
//            Mockito.when(s3Uploader.upload(multipartFile, "projectThumbnail")).thenReturn("thumbnailUrl");
//            Mockito.when(s3Uploader.upload(infoFile1, "projectInfo")).thenReturn("infoFileUrl1");
//            Mockito.when(s3Uploader.upload(infoFile2, "projectInfo")).thenReturn("infoFileUrl2");
//
//            // when
//            Long savedProjectId = projectService.createProject(requestDto, multipartFile, infoFiles);
//            System.out.println(savedProjectId);
//            // then
//            assertTrue(savedProjectId > 0L);
//        }
//    }
//
//    @Nested
//    @DisplayName("???????????? ????????????")
//    @Order(2)
//    class GetProjects{
//        @DisplayName("????????? ??????(??????)")
//        @Test
//        void getProjects() {
//            //given
//            String search = null;
//            String language = null;
//            String genre = null;
//            String step = null;
//            int page = 0;
//            String sorted = "createdDate";
//            //when
//            ProjectDto.Slice result = projectService.getProjects(
//                    search, language,genre, step, token, page, sorted
//            );
//            //then
//            assertNotNull(result.getList());
//            assertTrue(result.getList().size()>0);
//        }
//        @DisplayName("1??? ??????")
//        @Test
//        void getProject() {
//            //given
//            Long projectId = project.getId();
//            //when
//            ProjectDto.Response result = projectService.getProject(projectId);
//            //then
//            assertNotNull(result);
//            assertEquals(project.getProjectName(), result.getProjectName());
//        }
//        @DisplayName("???????????? ?????? ??????")
//        @Test
//        void getProjectMain() {
//            //given
//            Long projectId = project.getId();
//            //when
//            ProjectDto.Response result = projectService.getProjectMain(projectId);
//            //then
//            assertNotNull(result);
//            assertEquals(project.getProjectName(), result.getProjectName());
//        }
//
//    }
//    @Nested
//    @DisplayName("?????? ???????????? ?????????")
//    class GetMyProjects{
//        @DisplayName("??????")
//        @Test
//        void getMyProjects() {
//            //given
//            //when
//            List<ProjectDto.Response> results = projectService.getMyProjects();
//            //then
//            assertNotNull(results);
//            assertTrue(results.size()>0);
//            assertEquals(project.getProjectName(), results.get(0).getProjectName());
//        }
//
//    }
//    @Nested
//    @DisplayName("???????????? ???")
//    class ProjectZzim{
//        @DisplayName("??? ??????")
//        @Test
//        void projectZzim() {
//            //given
//            Long projectId = project.getId();
//            //when
//            projectService.projectZzim(projectId);
//            //then
//            assertTrue(zzimRepository.existsByProjectAndUser(project,user));
//        }
//        @DisplayName("??? ??????")
//        @Test
//        void projectZzimDelete() {
//            //given
//            Long projectId = project.getId();
//            //when
//            projectService.projectZzim(projectId);
//            projectService.projectZzim(projectId);
//            //then
//            assertFalse(zzimRepository.existsByProjectAndUser(project,user));
//        }
//
//    }
//    @Nested
//    @DisplayName("???????????? ??????")
//    class ModifyProject{
//        @DisplayName("??????")
//        @Test
//        void modifyProject() {
//            // given
//            Long projectId = project.getId();
//            ProjectDto.Request requestDto = ProjectDto.Request.builder()
//                    .projectName("projectName_??????")
//                    .projectDescription("projectDescription_??????")
//                    .feCount(2)
//                    .beCount(2)
//                    .deCount(2)
//                    .github("http://github.com")
//                    .figma("http://figma.com")
//                    .deadLine(LocalDate.now().plusDays(1))
//                    .step("??????")
//                    .language(new ArrayList<>(Arrays.asList("java", "python")))
//                    .genre(new ArrayList<>(Arrays.asList("???", "??????")))
//                    .build();
//            MockMultipartFile multipartFile = new MockMultipartFile("data", "multipartFile", "img", "multipartFile".getBytes());
//            Mockito.when(s3Uploader.upload(multipartFile, "projectThumbnail")).thenReturn("thumbnailUrlNew");
//
//            // when
//            ProjectDto.Response result = projectService.modifyProject(projectId, requestDto, multipartFile);
//            // then
//            Project modifiedProject = projectRepository.findById(projectId).get();
//            assertEquals(requestDto.getProjectName(),result.getProjectName());
//            assertEquals(requestDto.getProjectName(),modifiedProject.getProjectName());
//            assertEquals("thumbnailUrlNew",result.getThumbnail());
//            assertEquals("thumbnailUrlNew",modifiedProject.getThumbnail());
//        }
//        @DisplayName("infofile1 -> infoFileNew ?????? ??????")
//        @Test
//        void modifyInfoFile() {
//            //given
//            Long projectId = project.getId();
//            MockMultipartFile multipartFile = new MockMultipartFile("data", "multipartFile", "img", "multipartFile".getBytes());
//            MockMultipartFile infoFileNew = new MockMultipartFile("data", "infoFileNew", "img", "infoFileNew".getBytes());
//            Mockito.doNothing().when(s3Uploader).deleteFromS3(Mockito.anyString());
//            Mockito.when(s3Uploader.upload(infoFileNew, "projectInfo")).thenReturn("infoFileNewUrl");
//            //when
//            String infoFileNewUrl = projectService.modifyInfoFile(projectId,"infoFile1",infoFileNew);
//            //then
//            assertFalse(projectRepository.findById(projectId).get().getInfoFiles().contains("infoFile1"));
//            assertTrue(projectRepository.findById(projectId).get().getInfoFiles().contains("infoFileNewUrl"));
//            assertEquals("infoFileNewUrl",infoFileNewUrl);
//
//        }
//        @DisplayName("?????? ??????")
//        @Test
//        void deleteProject() {
//            //given
//            Long projectId = project.getId();
//            //when
//            projectService.deleteProject(projectId);
//            //then
//            assertFalse(projectRepository.existsById(projectId));
//        }
//
//    }
//    @Nested
//    @DisplayName("???????????? ?????? ??????")
//    class ProjectUser{
//        @DisplayName("?????? ?????? ?????? ??????")
//        @Test
//        void existUser() {
//            //given
//            Long projectId = project.getId();
//            //when
//            boolean result = projectService.existUser(projectId);
//            //then
//            assertFalse(result);
//        }
//        @DisplayName("?????? ??????")
//        @Test
//        void applyProject() {
//            //given
//            Long projectId = project.getId();
//            applyUser = userRepository.save(
//                    User.builder()
//                            .username("username_apply")
//                            .nickname("nickname_apply")
//                            .password("password")
//                            .language(Arrays.asList(
//                                    Language.builder().language("java").build(),
//                                    Language.builder().language("python").build()
//                            ))
//                            .profileUrl("profileUrl")
//                            .role("????????? ?????????")
//                            .github("github.com")
//                            .figma("figma.com")
//                            .intro("????????? ?????????")
//                            .email("test@test.com")
//                            .phoneNumber("01011112222")
//                            .status(true)
//                            .build()
//            );
//            token = jwtTokenProvider.createToken(applyUser.getUsername());
//            Authentication authentication = jwtTokenProvider.getAuthentication(token);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            //when
//            projectService.applyProject(projectId);
//            //then
//            assertTrue(userProjectRepository.existsByProjectAndUserAndIsTeam(project,applyUser,false));
//        }
//        @DisplayName("?????? ?????? ??????")
//        @Test
//        void addUser() {
//            //given
//            applyProject();
//            Long projectId = project.getId();
//            user = project.getUser();
//            token = jwtTokenProvider.createToken(user.getUsername());
//            Authentication authentication = jwtTokenProvider.getAuthentication(token);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            //when
//            projectService.addUser(projectId, applyUser.getId());
//            //then
//            assertTrue(userProjectRepository.existsByProjectAndUserAndIsTeam(project,applyUser,true));
//
//        }
//        @DisplayName("?????? ???????????? ??????")
//        @Test
//        void kickUser() {
//            //given
//            addUser();
//            Long projectId = project.getId();
//            //when
//            projectService.kickUser(projectId,applyUser.getId());
//            //then
//            assertFalse(userProjectRepository.existsByProjectAndUser(project,applyUser));
//        }
//    }
//}