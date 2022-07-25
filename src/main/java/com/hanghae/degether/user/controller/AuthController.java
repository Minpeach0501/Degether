package com.hanghae.degether.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hanghae.degether.user.dto.UserResponseDto;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.doc.repository.service.GoogleService;
import com.hanghae.degether.doc.repository.service.KakaoService;
import com.hanghae.degether.doc.repository.service.NaverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    private final KakaoService kakaoService;

    private final NaverService naverService;

    private final GoogleService googleService;

    private final UserRepository userRepository;

    @Autowired
    public AuthController(KakaoService kakaoService,
                          NaverService naverService,
                          UserRepository userRepository,
                          GoogleService googleService
    ){
        this.kakaoService = kakaoService;
        this.naverService =naverService;
        this.userRepository = userRepository;
        this.googleService =googleService;
    }
    //카카오 로그인
    @PostMapping ("/user/kakao")
    public UserResponseDto kakaoLogin(@RequestParam String redirectUrl, @RequestParam  String code, HttpServletResponse response) throws JsonProcessingException {
// authorizedCode: 카카오 서버로부터 받은 인가 코드
        return kakaoService.kakaoLogin(code,response,redirectUrl);
    }
    // 네이버 로그인
    @PostMapping("/user/naver")
    public UserResponseDto naverLogin(@RequestParam  String code, @RequestParam  String state, HttpServletResponse response ) throws JsonProcessingException {
        return naverService.naverLogin(code,state,response);
    }

    //구글 서비스 로그인
    @PostMapping("/user/google")
    public UserResponseDto googleLogin(@RequestParam String code, @RequestParam String state, HttpServletResponse response ) throws JsonProcessingException {
        return googleService.googleLogin(code,state,response);
    }

}
