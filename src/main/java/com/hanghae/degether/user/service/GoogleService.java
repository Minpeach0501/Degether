package com.hanghae.degether.user.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
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


    @Value("${google.client.id}")
    private String google_client_id;

    @Value("${google.key}")
    private String secret_key;

    @Autowired
    public GoogleService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder =passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // ?????? ?????????
    @Transactional
    public UserResponseDto googleLogin(String code, HttpServletResponse response, String redirectUrl) throws JsonProcessingException {

        // ??????????????? ??????????????? ????????????
        String accessToken = getAccessToken(code, redirectUrl);

        SocialUserInfoDto googleUserInfo = getUserInfo(accessToken);
        // ????????????????????? ???????????? ????????????
        User googleUser = registerKakaoUserIfNeed(googleUserInfo);

        googleUsersAuthorizationInput(googleUser, response);

        Optional<User> user = userRepository.findByUsername(googleUser.getUsername());

        LoginResDto loginResDto = new LoginResDto(user);

        return new UserResponseDto<>(true, "???????????????", loginResDto);
    }

    // ??????????????? ??????????????? ????????????
    public String getAccessToken(String code,  String redirectUrl) throws JsonProcessingException {

        // ????????? Content-type ??????
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // ????????? ????????? ?????? ??????
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", google_client_id);
        body.add("client_secret", secret_key);
        body.add("code", code);
        body.add("redirect_uri", redirectUrl);
        body.add("grant_type", "authorization_code");

        // POST ?????? ?????????
        HttpEntity<MultiValueMap<String, String>> googleToken = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST, googleToken,
                String.class
        );

        // response?????? ??????????????? ????????????
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseToken = objectMapper.readTree(responseBody);
        String accessToken = responseToken.get("access_token").asText();


        return accessToken;
    }

    // ????????????????????? ???????????? ????????????
    public SocialUserInfoDto getUserInfo(String accessToken) throws JsonProcessingException {

        // ????????? ??????????????? ??????, Content-type ??????
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer" + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // POST ?????? ?????????
        HttpEntity<MultiValueMap<String, String>> googleUser = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://openidconnect.googleapis.com/v1/userinfo",
                HttpMethod.POST, googleUser,
                String.class
        );

        // response ?????? ???????????? ????????????
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode googleUserInfo = objectMapper.readTree(responseBody);

        // ???????????? ??????

        String username = googleUserInfo.get("sub").asText();
        String nickname = googleUserInfo.get("name").asText();
        String profileUrl = "";
        try {
            profileUrl = googleUserInfo.get("picture").asText();
        }
        catch (NullPointerException e){
            profileUrl = "https://ossack.s3.ap-northeast-2.amazonaws.com/basicprofile.png";
        }

        log.debug("????????? ????????? ??????");
        log.debug("????????? : " + nickname);
        log.debug("????????? : " + username);
        log.debug("?????????????????? URL : " + profileUrl);

        return new SocialUserInfoDto(username,nickname,profileUrl);
    }
    // 3. email??? db ?????? ????????? ???????????? ??????
    private User registerKakaoUserIfNeed(SocialUserInfoDto googleUserInfo) {
        // DB ??? ????????? email??? ????????? ??????
        String username = "google"+googleUserInfo.getId();
        String nickname = googleUserInfo.getNickName();
        String profileUrl = googleUserInfo.getProfileUrl();
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            // ????????????
            // password: random UUID
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);

            user = new User(username, nickname, profileUrl, encodedPassword);
            userRepository.save(user);

        }
        return user;
    }

    private UserResponseDto googleUsersAuthorizationInput(User naverUser, HttpServletResponse response) {
        String token = jwtTokenProvider.createToken(naverUser.getUsername());
        // exception ??????????????? stauts ????????? ????????? ???????????? ?????????????????????
        // ????????? ??????????????? dto??? ??????

        if (naverUser.isStatus() == false) {
            token = null;
            throw new CustomException(ErrorCode.DELETED_USER);
        }
        response.addHeader("Authorization", token);
        return new UserResponseDto(true, "??????");

    }
}
