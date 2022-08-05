package com.hanghae.degether.user.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.project.model.UserProject;
import com.hanghae.degether.project.repository.ProjectRepository;
import com.hanghae.degether.project.repository.UserProjectRepository;
import com.hanghae.degether.user.dto.LoginResDto;
import com.hanghae.degether.user.dto.UserResponseDto;
import com.hanghae.degether.user.dto.SocialUserInfoDto;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class GoogleService   {


    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;


    @Value("${google.client.id}")
    private String google_client_id;

    @Value("${google.key}")
    private String secret_key;

    @Autowired
    public GoogleService(UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         JwtTokenProvider jwtTokenProvider,
                         ProjectRepository projectRepository, UserProjectRepository userProjectRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder =passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.projectRepository = projectRepository;
        this.userProjectRepository = userProjectRepository;
    }

    // 구글 로그인
    @Transactional
    public UserResponseDto googleLogin(String code, HttpServletResponse response, String redirectUrl) throws JsonProcessingException {

        // 인가코드로 엑세스토큰 가져오기
        String accessToken = getAccessToken(code, redirectUrl);

        SocialUserInfoDto googleUserInfo = getUserInfo(accessToken);
        // 엑세스토큰으로 유저정보 가져오기
        User googleUser = registerKakaoUserIfNeed(googleUserInfo);

        googleUsersAuthorizationInput(googleUser, response);

        Optional<User> user = userRepository.findByUsername(googleUser.getUsername());

        LoginResDto loginResDto = new LoginResDto(user);

        return new UserResponseDto<>(true, "로그인성공", loginResDto);
    }

    // 인가코드로 엑세스토큰 가져오기
    public String getAccessToken(String code,  String redirectUrl) throws JsonProcessingException {

        // 헤더에 Content-type 지정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 바디에 필요한 정보 담기
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", google_client_id);
        body.add("client_secret", secret_key);
        body.add("code", code);
        body.add("redirect_uri", redirectUrl);
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

        String username = googleUserInfo.get("sub").asText();
        String nickname = googleUserInfo.get("name").asText();
        String profileUrl = "";
        try {
            profileUrl = googleUserInfo.get("picture").asText();
        }
        catch (NullPointerException e){
            profileUrl = "https://ossack.s3.ap-northeast-2.amazonaws.com/basicprofile.png";
        }

        log.debug("로그인 이용자 정보");
        log.debug("닉네임 : " + nickname);
        log.debug("이메일 : " + username);
        log.debug("프로필이미지 URL : " + profileUrl);

        return new SocialUserInfoDto(username,nickname,profileUrl);
    }
    // 3. email로 db 유무 확인후 회원가입 처리
    private User registerKakaoUserIfNeed(SocialUserInfoDto googleUserInfo) {
        // DB 에 중복된 email이 있는지 확인
        String username = "google"+googleUserInfo.getId();
        String nickname = googleUserInfo.getNickName();
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

            // 이벤트 체험용 프로젝트
            Project project = projectRepository.findById(21261L).orElseThrow(()-> new CustomException(ErrorCode.UNAUTHORIZED));
            UserProject userProject = userProjectRepository.findByProjectAndUser(project, user).orElseThrow(()->
                    new CustomException(ErrorCode.NOT_APPLY)
            );
            if (userProject.isTeam()) {
                throw new CustomException(ErrorCode.DUPLICATED_JOIN);
            }
            userProjectRepository.save(UserProject.builder()
                    .user(user)
                    .project(project)
                    .isTeam(true)
                    .build());
            // 이벤트 체험용 프로젝트
        }
        return user;
    }

    private UserResponseDto googleUsersAuthorizationInput(User googleUser, HttpServletResponse response) {
        String token = jwtTokenProvider.createToken(googleUser.getUsername());
        // exception 발생시켜서 stauts 값으로 탈퇴한 회원들을 판별하기때문에
        // 토큰값 안넘겨주고 dto값 반환

        if (googleUser.isStatus() == false) {
            token = null;
            throw new CustomException(ErrorCode.DELETED_USER);
        }
        response.addHeader("Authorization", token);
        return new UserResponseDto(true, "성공");

    }
}
