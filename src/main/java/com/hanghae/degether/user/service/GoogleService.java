package com.hanghae.degether.user.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanghae.degether.user.dto.LoginResponseDto;
import com.hanghae.degether.user.dto.SocialUserInfoDto;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
@Slf4j
public class GoogleService   {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public GoogleService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder =passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 구글 로그인
    @Transactional
    public LoginResponseDto googleLogin(String code, String state, HttpServletResponse response) throws JsonProcessingException {

        // 인가코드로 엑세스토큰 가져오기
        String accessToken = getAccessToken(code, state);

        SocialUserInfoDto googleUserInfo = getUserInfo(accessToken);
        // 엑세스토큰으로 유저정보 가져오기
        User naverUser = registerKakaoUserIfNeed(googleUserInfo);


        return googleUsersAuthorizationInput(naverUser, response);

    }

    // 인가코드로 엑세스토큰 가져오기
    public String getAccessToken(String code, String state) throws JsonProcessingException {

        // 헤더에 Content-type 지정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 바디에 필요한 정보 담기
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", "");
        body.add("client_secret", "");
        body.add("code", code);
        body.add("redirect_uri", "");
        body.add("grant_type", "authorization_code");

        // POST 요청 보내기
        HttpEntity<MultiValueMap<String, String>> googleToken = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST, googleToken,
                String.class
        );

        // response에서 엑세스토큰 가져오기
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseToken = objectMapper.readTree(responseBody);
        String accessToken = responseToken.get("access_token").asText();


        return accessToken;
    }

    // 엑세스토큰으로 유저정보 가져오기
    public SocialUserInfoDto getUserInfo(String accessToken) throws JsonProcessingException {

        // 헤더에 엑세스토큰 담기, Content-type 지정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer" + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // POST 요청 보내기
        HttpEntity<MultiValueMap<String, String>> googleUser = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://openidconnect.googleapis.com/v1/userinfo",
                HttpMethod.POST, googleUser,
                String.class
        );

        // response 에서 유저정보 가져오기
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode googleUserInfo = objectMapper.readTree(responseBody);

        // 유저정보 작성
        String username = googleUserInfo.get("email").asText();
        String nickname = googleUserInfo.get("name").asText();
        String profileUrl = googleUserInfo.get("picture").asText();

        log.debug("로그인 이용자 정보");
        log.debug("닉네임 : " + nickname);
        log.debug("이메일 : " + username);
        log.debug("프로필이미지 URL : " + profileUrl);

        return new SocialUserInfoDto(nickname,username,profileUrl);
    }
    // 3. email로 db 유무 확인후 회원가입 처리
    private User registerKakaoUserIfNeed(SocialUserInfoDto googleUserInfo) {
        // DB 에 중복된 email이 있는지 확인
        String username = googleUserInfo.getEmail();
        String nickname = googleUserInfo.getNickname();
        String profileUrl = googleUserInfo.getProfileUrl();
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            // 회원가입
            // password: random UUID
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);

            user = new User(username, nickname, profileUrl, encodedPassword);
            userRepository.save(user);

        }
        return user;
    }

    private LoginResponseDto googleUsersAuthorizationInput(User naverUser, HttpServletResponse response) {
        String token = jwtTokenProvider.createToken(naverUser.getUsername());
        // exception 발생시켜서 stauts 값으로 탈퇴한 회원들을 판별하기때문에
        // 토큰값 안넘겨주고 dto값 반환
        try {
            if (naverUser.isStatus() == false) {
                token = null;
                throw new NullPointerException("탈퇴한 회원입니다.");
            }
            response.addHeader("Authorization", "BEARER" + " " + token);
            return new LoginResponseDto(true, "성공");
        } catch (NullPointerException e) {
            String message = e.getMessage();
            return new LoginResponseDto(false, message);
        }
    }
}
