package com.hanghae.degether.Service.User;


import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.project.util.S3Uploader;
import com.hanghae.degether.user.dto.MypageReqDto;
import com.hanghae.degether.user.dto.ProfileResDto;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.JwtTokenProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class MypageServiceTest {

    @MockBean
    S3Uploader s3Uploader;

    @MockBean
    ProfileResDto profileResDto;

    @Autowired
    UserRepository userRepository;

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


     User setupUser;
//     Project project;
     String token;




    @BeforeEach
    void setupUser(){
        setupUser = User.builder()
                .email("test@test.com")
                .password("testPassword")
                .nickname("저에요")
                .username("kakaoTest123456")
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
        userRepository.save(setupUser);

        token = jwtTokenProvider.createToken(setupUser.getUsername());
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
        User user = userRepository.findByUsername(setupUser.getUsername()).orElseThrow(
                ()->  new NullPointerException("없는 사용자 입니다.")
        );

        MypageReqDto reqDto = MypageReqDto.builder()
                .profileUrl("profileUrl")
                .role("백엔드 개발자")
                .nickname("수정중")
                .language(Collections.singletonList("java"))
                .github("gihub.com")
                .figma("figma.com")
                .intro("한줄소개 수정중")
                .phoneNumber("01000000000")
                .email("pub@sub.com")
                .build();



        String profileUrl = user.getProfileUrl();
        MockMultipartFile file = new MockMultipartFile("data","multipartFile", "img",  "multipartFile".getBytes());
        if (file != null){
            s3Uploader.deleteFromS3(s3Uploader.getFileName(user.getProfileUrl()));
            profileUrl = s3Uploader.upload(file,"userProfile");
        }

        List<Language> language = reqDto.getLanguage().stream().map((string) -> Language.builder().language(string).build()).collect(Collectors.toList());

        User updatedUser = User.builder()
                .profileUrl(profileUrl)
                .password(setupUser.getPassword())
                .role(reqDto.getRole())
                .nickname(reqDto.getNickname())
                .github(reqDto.getGithub())
                .figma(reqDto.getFigma())
                .intro(reqDto.getIntro())
                .language(language)
                .phoneNumber(reqDto.getPhoneNumber())
                .email(reqDto.getEmail())
                .build();

        userRepository.save(updatedUser);

        assertThat(reqDto.getProfileUrl().equals(updatedUser.getProfileUrl()));
        assertThat(reqDto.getEmail().equals(updatedUser.getEmail()));
        assertThat(reqDto.getIntro().equals(updatedUser.getIntro()));
    }

    @Test
    @Order(2)
    @DisplayName("회원 삭제")
    void deleteUser(){
        User user = new User();
         user = User.builder()
                 .email("test2@test.com")
                 .password("testPassword")
                 .nickname("접니다")
                 .username("kakaoTest22")
                 .language(Arrays.asList(
                         Language.builder().language("java").build(),
                         Language.builder().language("python").build()
                 ))
                 .profileUrl("https://ossack.s3.ap-northeast-2.amazonaws.com/basicprofile.png")
                 .role("백엔드 개발자")
                 .github("github.com")
                 .figma("figma.com")
                 .intro("안녕")
                 .phoneNumber("01011112222")
                 .status(true)
                 .build();

                 userRepository.save(user);

                 //우리 서비스는 회원가입시 유저의 status 값을 바꾼다
                 user.setStatus(false);

    }

    @Test
    @Order(3)
    @DisplayName("사용자 정보가져오기")
    void getOtherUserInfo(){


        User user2 =userRepository.findByUsername(setupUser.getUsername()).orElseThrow(
                () -> new NullPointerException("존재하지 않습니다.")
        );
        List<String> language = user2.getLanguage().stream().map(Language::getLanguage).collect(Collectors.toList());

        ProfileResDto profileResDto = ProfileResDto.builder()
                .profileUrl(user2.getProfileUrl())
                .role(user2.getRole())
                .nickname(user2.getNickname())
                .language(language)
                .github(user2.getGithub())
                .figma(user2.getFigma())
                .intro(user2.getIntro())
                .email(user2.getEmail())
                .phoneNumber(user2.getPhoneNumber())
                .build();

        assertThat(user2.getEmail().equals(profileResDto.getEmail()));
    }


}
