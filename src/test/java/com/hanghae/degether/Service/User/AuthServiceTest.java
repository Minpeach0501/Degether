//package com.hanghae.degether.Service.User;
//
//
//import com.hanghae.degether.project.model.Language;
//import com.hanghae.degether.user.model.User;
//import com.hanghae.degether.user.repository.UserRepository;
//import com.hanghae.degether.user.security.JwtTokenProvider;
//import io.jsonwebtoken.ExpiredJwtException;
//import io.jsonwebtoken.MalformedJwtException;
//import io.jsonwebtoken.SignatureException;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Arrays;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.Mockito.when;
//
//
//@SpringBootTest
//@Transactional
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@ExtendWith(MockitoExtension.class)
//class AuthServiceTest {
//
//    @Mock
//    UserRepository userRepository;
//    @Mock
//    PasswordEncoder passwordEncoder;
//    @Autowired
//    JwtTokenProvider jwtTokenProvider;
//
//
//
//    static String email = "test@test.com";
//    static String username = "kakaotest";
//    static String profileUrl = "https://ossack.s3.ap-northeast-2.amazonaws.com/basicprofile.png";
//    static String nickname = "test2";
//    static String intro = "저입니다";
//
//
//    static User setupUser;
//    static String token;
//    @BeforeEach
//      void setup(){
//
//        setupUser = User.builder()
//                .email(email)
//                .password("testPassword")
//                .nickname(nickname)
//                .username(username)
//                .language(Arrays.asList(
//                        Language.builder().language("java").build(),
//                        Language.builder().language("python").build()
//                ))
//                .profileUrl(profileUrl)
//                .role("백엔드 개발자")
//                .github("github.com")
//                .figma("figma.com")
//                .intro(intro)
//                .phoneNumber("01011112222")
//                .status(true)
//                .build();
//
//        token = jwtTokenProvider.createToken("username");
//    }
//
//
//    @Test
//    @Order(1)
//    @DisplayName("회원가입 테스트입니다!")
//    void createKakaoUser(){
//        String password = UUID.randomUUID().toString();
//        when(passwordEncoder.encode(password)).thenReturn("08180210");
//
//        String encodedPassword = passwordEncoder.encode(password);
//
//        User kakaoUser = User.builder()
//                .email(email)
//                .password(encodedPassword)
//                .nickname(nickname)
//                .username(username)
//                .profileUrl(profileUrl)
//                .intro(intro)
//                .build();
//
//        // username으로 찾아왔을때
//        when(userRepository.findByUsername(username)).thenReturn(Optional.ofNullable(kakaoUser));
//
//        Optional<User> findKakaoUser = userRepository.findByUsername(username);
//
//        Assertions.assertEquals(kakaoUser,findKakaoUser.get());
//
//    }
//
//
//
//
//
//    @Test
//    @Order(2)
//    @DisplayName("토큰유효성 검증 1.공백")
//    void validateToken( ){
//        String tokens = "";
////        jwtTokenProvider.validateToken(tokens)
//        assertThatThrownBy(() -> {
//            jwtTokenProvider.validateToken(tokens);
//                }).isInstanceOf(RuntimeException.class);
//
//    }
//
//    @Test
//    @Order(3)
//    @DisplayName("토큰유효성 검증 2.잘못퇸 형식")
//    void validateToken2(){
//        String tokens = "wrongssesdsasded";
//
//        assertThatThrownBy(() -> {
//            jwtTokenProvider.validateToken(tokens);
//        }).isInstanceOf(MalformedJwtException.class);
//
//    }
//
//    @Test
//    @Order(4)
//    @DisplayName("토큰유효성 검증  3. JWT 기존 서명오류 토큰 ")
//    void validateToken3(){
//        String tokens = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJza2oxNzA3QG5hdmVyLmNvbSIsImlhdCI6MTY1NjkwOTE2NywiZXhwIjoxNjU2OTk1NTY3fQ.27bFCLcIElRNt5PsMj271ArM3VYZhpZV6hKF5AGHDa4";
//
//            assertThatThrownBy(() -> {
//            jwtTokenProvider.validateToken(tokens);
//        }).isInstanceOf(SignatureException.class);
//
//
//    }
//
//    @Test
//    @Order(5)
//    @DisplayName("토큰유효성 검증  4. 만료된 토큰 ")
//    void validateToken4(){
//        String tokens = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJrYWthbzIzMzU3OTEyNzMiLCJpYXQiOjE2NTg4MzA0ODIsImV4cCI6MTY1ODgzMDQ4M30.OyJmUACPojwSwr1mrYGM-fDtOhbwP1z8ZQ4WCbg5kLE";
//
//        assertThatThrownBy(() -> {
//            jwtTokenProvider.validateToken(tokens);
//        }).isInstanceOf(ExpiredJwtException.class);
//
//
//    }
//
//
//}
//
