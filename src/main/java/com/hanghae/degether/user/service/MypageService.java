package com.hanghae.degether.user.service;

import com.hanghae.degether.doc.dto.ResponseDto;
import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.project.model.Zzim;
import com.hanghae.degether.project.repository.ProjectRepository;
import com.hanghae.degether.project.repository.UserProjectRepository;
import com.hanghae.degether.project.repository.ZzimRepository;
import com.hanghae.degether.project.util.S3Uploader;
import com.hanghae.degether.user.dto.*;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MypageService {

    private final ZzimRepository zzimRepository;

    private final UserProjectRepository userProjectRepository;

    private final UserRepository userRepository;

    private  final  S3Uploader s3Uploader;

    @Autowired
    public MypageService(ZzimRepository zzimRepository,
                         UserProjectRepository userProjectRepository,
                         UserRepository userRepository,
                         S3Uploader s3Uploader
    )
    {
        this.zzimRepository =zzimRepository;
        this.userProjectRepository =userProjectRepository;
        this.userRepository = userRepository;
        this.s3Uploader = s3Uploader;

    }

    @Transactional
    public MyPageResDto getuserInfo(MypageReqDto mypageReqDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String profileUrl = mypageReqDto.getProfileUrl();
        String role = mypageReqDto.getRole();
        String nickname = mypageReqDto.getNickname();
        List<Language> languages = mypageReqDto.getLanguages();
        String github = mypageReqDto.getGithub();
        String figma = mypageReqDto.getFigma();
        String intro = mypageReqDto.getIntro();

        User user = userDetails.getUser();

        // 내가  찜한 프로젝트 불러오기
        List<Zzim> Zzims = zzimRepository.findAllByUser(user);
        //  찜한 프로젝트에서 필요한 값만 담기
        List<ZzimResDto> Zzim = new ArrayList<>();

        for (Zzim zzim : Zzims) {
            ZzimResDto zzimResDto = new ZzimResDto(zzim);
            Zzim.add(zzimResDto);
        }

        // 내가 참여한 모든 프로 젝트들 불러오기
        List<MyProjectResDto> myproject = userProjectRepository.findAllByUserAndIsTeam(user, true);

        ResultDto resultDto = new ResultDto(profileUrl,role,nickname,languages,github,figma,intro,Zzim,myproject);

        return new MyPageResDto(true,"마이페이지 정보를 가져왔습니다.", resultDto);
    }


    @Transactional
    public LoginResponseDto deleteUser(UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        user.setStatus(false);
        userRepository.save(user);
        return new LoginResponseDto(true, "삭제성공");
    }
    @Transactional
    public ResponseDto<?> updateUserInfo(UserDetailsImpl userDetails, MultipartFile file, MypageReqDto reqDto){
        String username = userDetails.getUsername();
        String profileUrl = "";
        s3Uploader.deleteFromS3(profileUrl);

        Optional<User> user = userRepository.findByUsername(username);
        if(!user.isPresent()) {
            throw new IllegalArgumentException ("등록되지 않은 사용자입니다.");
        }
        if(!file.isEmpty()) {
            //이미지 업로드
            profileUrl = s3Uploader.upload(file, reqDto.getProfileUrl());
        }

        String phoneNumber = reqDto.getPhoneNumber();
        String figma = reqDto.getFigma();
        String github = reqDto.getGithub();
        String email = reqDto.getEmail();
        String nickname = reqDto.getNickname();
        String intro = reqDto.getIntro();
        String role = reqDto.getRole();
        List<Language> language = reqDto.getLanguages();

        MyUpdateDto myUpdateDto = new MyUpdateDto(profileUrl,role,nickname,language,github,figma,intro,phoneNumber,email);

        return  new ResponseDto<>(true,"수정 성공", myUpdateDto);
    }
}
