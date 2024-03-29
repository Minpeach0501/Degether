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
@RequiredArgsConstructor
public class KakaoService {


    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private final JwtTokenProvider jwtTokenProvider;
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;

    @Value("${kakao.client.id}")
    public String client_id;




    @Transactional
    public UserResponseDto kakaoLogin(String code, HttpServletResponse response, String redirectUrl) throws JsonProcessingException {
// 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code, redirectUrl);

// 2. "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        SocialUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);
// 3.유저가 등록된 유저가 아니면  회원가입
        User kakaouser = registerKakaoUserIfNeed(kakaoUserInfo);

        kakaoUsersAuthorizationInput(kakaouser, response);

        Optional<User> user = userRepository.findByUsername(kakaouser.getUsername());

        LoginResDto loginResDto = new LoginResDto(user);

       return new UserResponseDto<>(true, "로그인성공",loginResDto);

    }

    private String getAccessToken(String code, String redirectUrl) throws JsonProcessingException {
// HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

// HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", client_id);
        body.add("redirect_uri", redirectUrl);
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

    private SocialUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
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
        Long KaKaoid = jsonNode.get("id").asLong();
        String KaKaoUsername = jsonNode.get("id").asText();
        String KaKaoNickname = jsonNode.get("properties")
                .get("nickname").asText();
        String profileUrl = "";


        try {
            profileUrl = jsonNode.get("properties")
                    .get("profile_image").asText();
        }
        catch (NullPointerException e) {
            profileUrl = "https://ossack.s3.ap-northeast-2.amazonaws.com/basicprofile.png";
        }

        return new SocialUserInfoDto(KaKaoUsername ,KaKaoNickname, profileUrl);
    }
    // 3. 카카오ID로 회원가입 처리
    private User registerKakaoUserIfNeed (SocialUserInfoDto kakaoUserInfo) {
        // DB 에 중복된 email이 있는지 확인
        String username = "kakao"+kakaoUserInfo.getId();
        String nickname = kakaoUserInfo.getNickName();
        String profileUrl = kakaoUserInfo.getProfileUrl();
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            // 회원가입
            // password: random UUID
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);
//            String profile = "https://ossack.s3.ap-northeast-2.amazonaws.com/basicprofile.png";

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

    // 4. 강제 로그인 처리  >>> 필터 통과해야되기때문에
//    private Authentication forceLogin(User kakaoUser) {
//        UserDetails userDetails = new UserDetailsImpl(kakaoUser);
//        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        return authentication;
//    }

    // 5. response Header에 JWT 토큰 추가
    private UserResponseDto kakaoUsersAuthorizationInput(User kakaouser, HttpServletResponse response) {
        // response header에 token 추가
        String token = jwtTokenProvider.createToken(kakaouser.getUsername());

        try {
            if (kakaouser.isStatus() == false){
                token = null;
                throw  new CustomException(ErrorCode.DELETED_USER);
            }
            response.addHeader("Authorization", token);
            return new UserResponseDto(true,"성공");
        }catch (NullPointerException e) {
            String message = e.getMessage();
            return new UserResponseDto(false,message);
        }

    }
}