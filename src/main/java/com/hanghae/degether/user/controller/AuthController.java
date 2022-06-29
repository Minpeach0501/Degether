package com.hanghae.degether.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hanghae.degether.user.dto.LoginResponseDto;
import com.hanghae.degether.user.service.KakaoService;
import com.hanghae.degether.user.service.NaverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    private KakaoService kakaoService;

    private NaverService naverService;

    @Autowired
    public AuthController(KakaoService kakaoService,
                          NaverService naverService
    ){
        this.kakaoService = kakaoService;
        this.naverService =naverService;
    }

    @PostMapping ("/user/kakao/{code}")
    public LoginResponseDto kakaoLogin(@PathVariable String code, HttpServletResponse response) throws JsonProcessingException {
// authorizedCode: 카카오 서버로부터 받은 인가 코드
        return kakaoService.kakaoLogin(code,response);
    }
    @PostMapping("/user/naver/{code}/{state}")
    public  LoginResponseDto naverLogin(@PathVariable String code,@PathVariable String state,HttpServletResponse response ) throws JsonProcessingException {
        return naverService.naverLogin(code,state,response);
    }

}
