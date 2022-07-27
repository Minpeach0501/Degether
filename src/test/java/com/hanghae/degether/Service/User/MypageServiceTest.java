package com.hanghae.degether.Service.User;


import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.project.util.S3Uploader;
import com.hanghae.degether.user.dto.MypageReqDto;
import com.hanghae.degether.user.dto.ProfileResDto;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.JwtTokenProvider;
import com.hanghae.degether.user.service.MypageService;
import com.hanghae.degether.websocket.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Rollback
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class MypageServiceTest {

    @MockBean
    S3Uploader s3Uploader;

    @MockBean
    ProfileResDto profileResDto;

    @Autowired
    UserRepository userRepository2;

    @Autowired
    RedisConfig redisConfig;
//    @Autowired
//    UserProjectRepository userProjectRepository;
//
//    @Autowired
//    ZzimRepository zzimRepository;
//
//    @Autowired
//    ProjectRepository projectRepository;
//
//    @Autowired
//    ProjectService projectService;


    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    MypageService mypageService;


     User setupUser;
//     Project project;
     String token;




    @BeforeEach
    void setupUser(){
        String username = UUID.randomUUID().toString();
        setupUser = User.builder()
                .email("test@test.com")
                .password("testPassword")
                .nickname("저에요")
                .username(username)
                .language(Arrays.asList(
                        Language.builder().language("java").build(),
                        Language.builder().language("python").build()
                ))
                .profileUrl("https://ossack.s3.ap-northeast-2.amazonaws.com/basicprofile.png")
                .role("백엔드 개발자")
                .github("github.com")
                .figma("figma.com")
                .intro("안녕222")
                .phoneNumber("01012345678")
                .status(true)
                .build();
        userRepository2.save(setupUser);

        String token = jwtTokenProvider.createToken(setupUser.getUsername());
        log.info(token);
        Authentication authentication =jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);


//        UserProject userProject = UserProject.builder()
//                .isTeam(true)
//                .user(setupUser)
//                .build();
//
//        project = projectRepository.save(
//                Project.builder()
//                        .thumbnail("thumbnail")
//                        .projectName("projectName")
//                        .projectDescription("projectDescription")
//                        .feCount(2)
//                        .beCount(2)
//                        .deCount(2)
//                        .github("http://github1.com")
//                        .figma("http://figma1.com")
//                        .deadLine(LocalDate.now().plusDays(1))
//                        .step("기획")
//                        .languages(new ArrayList<>(Arrays.asList(
//                                        Language.builder().language("java").build(),
//                                        Language.builder().language("java").build()
//                                )
//                                )
//                        )
//                        .genres(new ArrayList<>(Arrays.asList(
//                                        Genre.builder().genre("앱").build(),
//                                        Genre.builder().genre("게임").build()
//                                )
//                                )
//                        )// .userProjects(userProjects)
//                        .userProjects(new ArrayList<>(Arrays.asList(userProject)))
//                        .user(setupUser)
//                        .infoFiles(new ArrayList<>(Arrays.asList("infoFile1","infoFile2")))
//                        .comments(Collections.emptyList())
//                        .build()
//        );
//
//        projectService.projectZzim(project.getId());

    }



    @Test
    @Order(1)
    @DisplayName("마이페이지 정보수정")
    void updateUserInfo(){

        User user = userRepository2.findByUsername(setupUser.getUsername()).orElseThrow(
                ()->  new NullPointerException("없는 사용자 입니다.")
        );


        MypageReqDto reqDto = MypageReqDto.builder()
                .profileUrl("profileUrl")
                .role("백엔드 개발자")
                .nickname("수정중")
                .language(new ArrayList<>(Arrays.asList("java", "python")))
                .github("gihub.com")
                .figma("figma.com")
                .intro("한줄소개 수정중")
                .phoneNumber("01000000000")
                .email("pub@sub.com")
                .build();


        String profileUrl = user.getProfileUrl();
        MockMultipartFile file = new MockMultipartFile("data","multipartFile", "img",  "multipartFile".getBytes());

        mypageService.updateUserInfo(user,file,reqDto);

        assertThat(reqDto.getProfileUrl().equals(user.getProfileUrl()));
        assertThat(reqDto.getEmail().equals(user.getEmail()));
        assertThat(reqDto.getIntro().equals(user.getIntro()));
    }

    @Test
    @Order(2)
    @DisplayName("회원 삭제")
    void deleteUser(){

        mypageService.deleteUser(setupUser);

        Optional<User> savedUser = userRepository2.findByUsername(setupUser.getUsername());


        assertThat(savedUser.get().isStatus() == false);

    }

    @Test
    @Order(3)
    @DisplayName("사용자 정보가져오기")
    void getOtherUserInfo(){


        String username = setupUser.getUsername();

        mypageService.OneUserInfo(username);
        User user = userRepository2.findByUsername(username).orElseThrow(
                ()-> new NullPointerException("없음")
        );


        assertThat(setupUser.getPhoneNumber().equals(profileResDto.getEmail()));
    }


}
