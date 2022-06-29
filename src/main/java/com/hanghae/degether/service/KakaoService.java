package com.hanghae.degether.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanghae.degether.dto.LoginResponseDto;
import com.hanghae.degether.dto.KakaoUserInfoDto;
import com.hanghae.degether.model.User;
import com.hanghae.degether.repository.UserRepository;
import com.hanghae.degether.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
public class KakaoService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private final JwtTokenProvider jwtTokenProvider;


    @Autowired
    public KakaoService(UserRepository userRepository, PasswordEncoder passwordEncoder,JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider=jwtTokenProvider;
    }


    public LoginResponseDto kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
// 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code);

// 2. "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);

        User kakaouser = registerKakaoUserIfNeed(kakaoUserInfo);



        kakaoUsersAuthorizationInput(kakaouser, response);

        return  new LoginResponseDto(true,"성공");
    }

    private String getAccessToken(String code) throws JsonProcessingException {
// HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

// HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "d184a94d70ebafe24481f7c3c707e788");
        body.add("redirect_uri", "http://localhost:3000/auth/kakao/callback");
        body.add("code", code);

// HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

// HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
// HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

// HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String username = jsonNode.get("kakao_account")
                .get("email").asText();
        String profielUrl = jsonNode.get("properties")
                .get("profile_image").asText();

        return new KakaoUserInfoDto(id, nickname, username, profielUrl);
    }
    // 3. 카카오ID로 회원가입 처리
    private User registerKakaoUserIfNeed (KakaoUserInfoDto kakaoUserInfo) {
        // DB 에 중복된 email이 있는지 확인
        String username = kakaoUserInfo.getEmail();
        String nickname = kakaoUserInfo.getNickname();
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            // 회원가입
            // password: random UUID
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);

            String profile = "https://ossack.s3.ap-northeast-2.amazonaws.com/basicprofile.png";

            user = new User(username, nickname, profile, encodedPassword);
            userRepository.save(user);

        }
        return user;
    }

    // 4. 강제 로그인 처리  >>> 필터 통과해야되기때문에
//    private Authentication forceLogin(User kakaoUser) {
//        UserDetails userDetails = new UserDetailsImpl(kakaoUser);
//        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        return authentication;
//    }

    // 5. response Header에 JWT 토큰 추가
    private void kakaoUsersAuthorizationInput(User kakaouser, HttpServletResponse response) {
        // response header에 token 추가

        String token = jwtTokenProvider.createToken(kakaouser.getUsername());
//        response.addHeader("Authorization", "BEARER" + " " + token);

    }
}