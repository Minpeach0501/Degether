package com.hanghae.degether.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hanghae.degether.dto.LoginResponseDto;
import com.hanghae.degether.service.KakaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    private KakaoService kakaoService;

    @Autowired
    public AuthController(KakaoService kakaoService){
        this.kakaoService = kakaoService;
    }

    @PostMapping ("/user/kakao/{code}")
    public LoginResponseDto kakaoLogin(@PathVariable String code, HttpServletResponse response) throws JsonProcessingException {
// authorizedCode: 카카오 서버로부터 받은 인가 코드
        return kakaoService.kakaoLogin(code,response);
    }




}
