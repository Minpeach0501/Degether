package com.hanghae.degether.user.controller;


import com.hanghae.degether.user.dto.MypageReqDto;
import com.hanghae.degether.user.dto.UserResponseDto;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.UserDetailsImpl;
import com.hanghae.degether.user.service.MypageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class MyPageController {

    private final MypageService mypageService;

    private final UserRepository userRepository;



    @Autowired
    public MyPageController(
            MypageService mypageService,
            UserRepository userRepository
    )
    {
        this.mypageService =mypageService;
        this.userRepository = userRepository;
    }


    // 회원탈퇴 우리는 삭제를 하지 않고 status 값만 바꿔 표시를 안해준다
    @PutMapping("/user/userDelete")
    public UserResponseDto deleteUser(@AuthenticationPrincipal UserDetailsImpl userDetails)
    {
        return mypageService.deleteUser(userDetails);
    }


   // 모든 마이페이지 정보 전달
    @GetMapping("/user/userInfo")
    public UserResponseDto<?> getUserInfo(MypageReqDto mypageReqDto, @AuthenticationPrincipal UserDetailsImpl userDetails)
    {
       return mypageService.getuserInfo(mypageReqDto, userDetails);
    }
    //정보 수정
    @PutMapping("/user/userEdit")
    public UserResponseDto<?> updateUserInfo(
            UserDetailsImpl userDetails,
            @RequestPart(value = "file") MultipartFile  file,
            @RequestPart(value = "updateDto") MypageReqDto reqDto
    ){

        return mypageService.updateUserInfo(userDetails,file,reqDto);
    }

    // 프로젝트 메인 페이지에서 프로필 보는용
    @GetMapping("user/userInfo/{username}")
    public UserResponseDto<?> getSelectUserInfo(@PathVariable String username)
    {
      return   mypageService.OneUserInfo(username);
    }


}
