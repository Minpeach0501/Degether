package com.hanghae.degether.Service.User;


import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.user.dto.ProfileResDto;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class MypageServiceTest {

    @Autowired
    UserRepository userRepository;

    @Mock
    ProfileResDto profileResDto;


     User setupOtherUser;





    @BeforeEach
    void setupOtherUser(){
        setupOtherUser = User.builder()
                .email("test@test.com")
                .password("testPassword")
                .nickname("저에요")
                .username("kakaoTest32323232323232323")
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

        userRepository.save(setupOtherUser);
    }



    @Test
    @Order(1)
    @DisplayName("마이페이지 정보수정")
    void updateUserInfo(){

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
        User user2 =userRepository.findByUsername(setupOtherUser.getUsername()).orElseThrow(
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
