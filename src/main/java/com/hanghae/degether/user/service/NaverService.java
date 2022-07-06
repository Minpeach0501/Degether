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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
@Slf4j
public class NaverService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public NaverService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder =passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }



    @Transactional
    public LoginResponseDto naverLogin(String code, String state, HttpServletResponse response) throws JsonProcessingException {
        //todo 프론트에서 받은 인가코드를 기반으로 인증서버에게 인증 받고,
        // 인증받은 사용자의 정보를 이용하여 SocialUserInfoDto를 생성하여 반환한다.
        String accessToken = getAccessToken(code,state);
        //프론트에서 받은 인가코드를 기반으로 인증서버에게 인증 받고,
        SocialUserInfoDto naverUserInfo = getUserInfo(accessToken);

        User naverUser = registerKakaoUserIfNeed(naverUserInfo);


        return naverUsersAuthorizationInput(naverUser, response);


//인증받은 사용자의 정보를 이용하여 LoginResponse 를 생성하여 반환한다.


    }

    // 1. "인가 코드"로 "액세스 토큰" 요청 (네이버는 state를 추가로 사용한다)
    public String getAccessToken(String code, String state) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성 (네이버는 secret key가 필요하다)
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "7AYfB5Qp7evMA4RqbOMh");
        body.add("client_secret", "q8HuZCWivT");
        body.add("redirect_uri", "http://localhost:3000/auth/naver/callback");
        body.add("code", code);
        body.add("state", state);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> naverTokenRequest = new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        try {
            ResponseEntity<String> response = rt.exchange(
                    "https://nid.naver.com/oauth2.0/token",
                    HttpMethod.POST,
                    naverTokenRequest,
                    String.class
            );

            // HTTP 응답 (JSON) -> 액세스 토큰 파싱
            String responseBody = response.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("access_token").asText();



        } catch (HttpClientErrorException e) {
            throw new IllegalArgumentException("오류입니다");
        }
    }

    // 2. "액세스 토큰"으로 "네이버 사용자 정보" 가져오기
    public SocialUserInfoDto getUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization",  accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> naverUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.POST,
                naverUserInfoRequest,
                String.class
        );
        // jackson 라이브러리의 JsonNode를 사용하여 응답을 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        System.out.println(responseBody);
        Long naverid = jsonNode.get("response").get("id").asLong();
        String nickname = jsonNode.get("response").get("nickname").asText();
        String username = jsonNode.get("response").get("email").asText();
        String profileUrl = jsonNode.get("response").get("profile_image").asText();


//        log.info("로그인 이용자 정보");
//        log.info("네이버 고유ID : " + id);
//        log.info("닉네임 : " + nickname);
//        log.info("이메일 : " + username);
//        log.info("프로필이미지 URL : " + profileUrl);

        return new SocialUserInfoDto(naverid,nickname, username, profileUrl);
    }

    // 3. email로 db 유무 확인후 회원가입 처리
    private User registerKakaoUserIfNeed(SocialUserInfoDto naverUserInfo) {
        // DB 에 중복된 email이 있는지 확인
        String username = ("naver" + String.valueOf(naverUserInfo.getId()));
        String nickname = naverUserInfo.getNickname();
        String profileUrl = naverUserInfo.getProfileUrl();
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



    // 4. response Header에 JWT 토큰 추가
    private LoginResponseDto naverUsersAuthorizationInput(User naverUser, HttpServletResponse response) {
        String token = jwtTokenProvider.createToken(naverUser.getUsername());
        // exception 발생시켜서 stauts 값으로 탈퇴한 회원들을 판별하기때문에
        // 토큰값 안넘겨주고 dto값 반환

        if (naverUser.isStatus() == false) {
            token = null;
            throw new IllegalArgumentException("탈퇴한 회원입니다.");
        }
        response.addHeader("Authorization", token);
        return new LoginResponseDto(true, "성공");

    }
}
