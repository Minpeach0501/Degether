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
import com.hanghae.degether.user.dto.SocialUserInfoDto;
import com.hanghae.degether.user.dto.UserResponseDto;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NaverService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;

    @Value("${naver.client.id}")
    private String naver_client_id;

    @Value("${naver.key}")
    private String secret_key;



    @Transactional
    public UserResponseDto naverLogin(String code, String state, HttpServletResponse response, String redirectUrl) throws JsonProcessingException {
        //todo 프론트에서 받은 인가코드를 기반으로 인증서버에게 인증 받고,
        // 인증받은 사용자의 정보를 이용하여 SocialUserInfoDto를 생성하여 반환한다.
        String accessToken = getAccessToken(code,state,redirectUrl);
        //프론트에서 받은 인가코드를 기반으로 인증서버에게 인증 받고,
        SocialUserInfoDto naverUserInfo = getUserInfo(accessToken);

        User naverUser = registernaverUserIfNeed(naverUserInfo);

        naverUsersAuthorizationInput(naverUser, response);

        Optional<User> user = userRepository.findByUsername(naverUser.getUsername());

        LoginResDto loginResDto = new LoginResDto(user);

        return  new UserResponseDto<>(true,"성공", loginResDto);

    }

    // 1. "인가 코드"로 "액세스 토큰" 요청 (네이버는 state를 추가로 사용한다)
    public String getAccessToken(String code, String state,String redirectUrl) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성 (네이버는 secret key가 필요하다)
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", naver_client_id);
        body.add("client_secret", secret_key);
        body.add("redirect_uri", redirectUrl);
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
            throw new CustomException(ErrorCode.NAVER_TOKEN);
        }
    }

    // 2. "액세스 토큰"으로 "네이버 사용자 정보" 가져오기
    public SocialUserInfoDto getUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
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

        String NaverUsername = jsonNode.get("response").get("id").asText();
        String NaverNickname = jsonNode.get("response").get("nickname").asText();
        String profileUrl = "";
        try {
             profileUrl = jsonNode.get("response").get("profile_image").asText();
        }catch (NullPointerException e){
            profileUrl = "https://ossack.s3.ap-northeast-2.amazonaws.com/basicprofile.png";
        }



//        log.info("로그인 이용자 정보");
//        log.info("네이버 고유ID : " + id);
//        log.info("닉네임 : " + nickname);
//        log.info("이메일 : " + username);
//        log.info("프로필이미지 URL : " + profileUrl);

        return new SocialUserInfoDto(NaverUsername,NaverNickname, profileUrl);
    }

    // 3. email로 db 유무 확인후 회원가입 처리
    private User registernaverUserIfNeed(SocialUserInfoDto naverUserInfo) {
        // DB 에 중복된 username이 있는지 확인
        //email은 선택동의라 선택하지않으면 username이  null값으로 들어가버림
        String username = "naver"+naverUserInfo.getId();
        String nickname = naverUserInfo.getNickName();
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
            // 이벤트 체험용 프로젝트

            Project project = projectRepository.findById(21261L).orElseThrow(()-> new CustomException(ErrorCode.UNAUTHORIZED));
            userProjectRepository.save(UserProject.builder()
                    .user(user)
                    .project(project)
                    .isTeam(true)
                    .build());
            // 이벤트 체험용 프로젝트

        }
        return user;
    }


    // 4. response Header에 JWT 토큰 추가
    private UserResponseDto naverUsersAuthorizationInput(User naverUser, HttpServletResponse response) {
        String token = jwtTokenProvider.createToken(naverUser.getUsername());
        // exception 발생시켜서 stauts 값으로 탈퇴한 회원들을 판별하기때문에
        // 토큰값 안넘겨주고 dto값 반환

        if (naverUser.isStatus() == false) {
            token = null;
            throw new CustomException(ErrorCode.DELETED_USER);
        }
        response.addHeader("Authorization", token);
        return new UserResponseDto(true, "성공");

    }
}
