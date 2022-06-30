package com.hanghae.degether.user.controller;


import com.hanghae.degether.user.dto.LoginResponseDto;
import com.hanghae.degether.user.dto.MyPageResDto;
import com.hanghae.degether.user.dto.MypageReqDto;
import com.hanghae.degether.user.security.UserDetailsImpl;
import com.hanghae.degether.user.service.MypageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyPageController {

    private final MypageService mypageService;

    @Autowired
    public MyPageController(MypageService mypageService){
        this.mypageService =mypageService;
    }


    // 회원탈퇴 우리는 삭제를 하지 않고 status 값만 바꿔 표시를 안해준다
    @PutMapping("/user/userDelete")
    public LoginResponseDto deleteUser(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return mypageService.deleteUser(userDetails);
    }


   // 모든 마이페이지 정보 전달
    @GetMapping("/user/userinfo")
    public MyPageResDto getuserInfo(MypageReqDto mypageReqDto, @AuthenticationPrincipal UserDetailsImpl userDetails){
       return mypageService.getuserInfo(mypageReqDto, userDetails);
    }
}
