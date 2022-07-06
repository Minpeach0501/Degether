package com.hanghae.degether.user.controller;


import com.hanghae.degether.user.dto.LoginResponseDto;
import com.hanghae.degether.user.dto.MyPageResDto;
import com.hanghae.degether.user.dto.MyUpdateDto;
import com.hanghae.degether.user.dto.MypageReqDto;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.UserDetailsImpl;
import com.hanghae.degether.user.service.MypageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class MyPageController {

    private final MypageService mypageService;

    private final UserRepository userRepository;



    @Autowired
    public MyPageController(MypageService mypageService,
                            UserRepository userRepository
    )
    {
        this.mypageService =mypageService;
        this.userRepository = userRepository;
    }


    // 회원탈퇴 우리는 삭제를 하지 않고 status 값만 바꿔 표시를 안해준다
    @PutMapping("/user/userDelete")
    public LoginResponseDto deleteUser(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return mypageService.deleteUser(userDetails);
    }


   // 모든 마이페이지 정보 전달
    @GetMapping("/user/userinfo")
    public MyPageResDto getUserInfo(MypageReqDto mypageReqDto, @AuthenticationPrincipal UserDetailsImpl userDetails){
       return mypageService.getuserInfo(mypageReqDto, userDetails);
    }
    //정보 수정
    @PutMapping("/user/userEdit")
    public List<MyUpdateDto> updateUserInfo(UserDetailsImpl userDetails,
                                            @RequestPart(value = "file") MultipartFile  file,
                                            @RequestPart(value = "updateDto") MypageReqDto reqDto){

        return mypageService.updateUserInfo(userDetails,file,reqDto);
    }
}
