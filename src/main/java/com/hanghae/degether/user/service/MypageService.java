package com.hanghae.degether.user.service;

import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.project.model.UserProject;
import com.hanghae.degether.project.model.Zzim;
import com.hanghae.degether.project.repository.UserProjectRepository;
import com.hanghae.degether.project.repository.ZzimRepository;
import com.hanghae.degether.project.util.S3Uploader;
import com.hanghae.degether.user.dto.*;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.IllegalInstantException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MypageService {

    private final ZzimRepository zzimRepository;

    private final UserProjectRepository userProjectRepository;

    private final UserRepository userRepository;

    private final S3Uploader s3Uploader;

    @Autowired
    public MypageService(
            ZzimRepository zzimRepository,
            UserProjectRepository userProjectRepository,
            UserRepository userRepository,
            S3Uploader s3Uploader
    ) {
        this.zzimRepository = zzimRepository;
        this.userProjectRepository = userProjectRepository;
        this.userRepository = userRepository;
        this.s3Uploader = s3Uploader;

    }

    @Transactional
    public UserResponseDto<?> getuserInfo(MypageReqDto mypageReqDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userDetails.getUser();


        List<Zzim> Zzims = zzimRepository.findAllByUser(user);

        List<ZzimResDto> Zzim = new ArrayList<>();

        for (Zzim zzim : Zzims) {
            ZzimResDto zzimResDto = new ZzimResDto(zzim);
            Zzim.add(zzimResDto);
        }
        List<MyProjectResDto> myProjectResDtos = new ArrayList<>();

        // 내가 참여한 모든 프로 젝트들 불러오기 projection 사용
        List<UserProject> myproject = userProjectRepository.findTop3ByUserAndIsTeam(user, true);

        for (UserProject userProject : myproject) {
            MyProjectResDto myProjectResDto1 = new MyProjectResDto(userProject);
            myProjectResDtos.add(myProjectResDto1);
        }

        List<String> language = user.getLanguage().stream().map(Language::getLanguage).collect(Collectors.toList());

        ResultDto resultDto = ResultDto.builder()
                .profileUrl(user.getProfileUrl())
                .role(user.getRole())
                .nickname(user.getNickname())
                .language(language)
                .github(user.getGithub())
                .figma(user.getFigma())
                .intro(user.getIntro())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .zzim(Zzim)
                .myProject(myProjectResDtos)
                .build();

        return new UserResponseDto<>(true, "마이페이지 정보를 가져왔습니다.", resultDto);
    }


    @Transactional
    public UserResponseDto deleteUser(UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        user.setStatus(false);
        userRepository.save(user);
        return new UserResponseDto(true, "삭제성공");
    }

    @Transactional
    public UserResponseDto<?> updateUserInfo(UserDetailsImpl userDetails, MultipartFile file, MypageReqDto reqDto) {
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalInstantException("등록되지 않은 사용자입니다.")
        );

        String profileUrl = user.getProfileUrl();

        if (file!=null) {
            //이미지 업로드
            s3Uploader.deleteFromS3(s3Uploader.getFileName(user.getProfileUrl()));
            profileUrl = s3Uploader.upload(file, reqDto.getProfileUrl());
        }

        // 프론트쪽으로 정보를 줄때에는 language라는 엔티티대신 스트링값을 줘야한다.
        List<Language> language = reqDto.getLanguage().stream().map((string) -> Language.builder().language(string).build()).collect(Collectors.toList());
        String nickname = reqDto.getNickname();
        String intro = reqDto.getIntro();

//        int nicknameL = nickname.length();
//        int introL = intro.length();

//        유효성검사는 validation으로 교체
//        if (nicknameL > 10) {
//            throw new IllegalArgumentException("글자수가 초과되었습니다.");
//        }
//        if (nicknameL < 2) {
//            throw new IllegalArgumentException("글자수가 부족합니다.");
//        }
//        if (introL > 20) {
//            throw new IllegalArgumentException("글자수가 초과되었습니다.");
//        }


        LoginResDto resDto = LoginResDto.builder()
                .userId(user.getId())
                .username(username)
                .profileUrl(profileUrl)
                .role(reqDto.getRole())
                .nickname(reqDto.getNickname())
                .language(reqDto.getLanguage())
                .github(reqDto.getGithub())
                .figma(reqDto.getFigma())
                .intro(reqDto.getIntro())
                .phoneNumber(reqDto.getPhoneNumber())
                .email(reqDto.getEmail())
                .build();

        user.update(resDto.getProfileUrl(),
                resDto.getRole(),
                resDto.getNickname(),
                language,
                resDto.getGithub(),
                resDto.getFigma(),
                resDto.getIntro(),
                resDto.getPhoneNumber(),
                resDto.getEmail());

        // 트랜잭션때문에 안써도 됌
        //userRepository.save(user.get());

        return new UserResponseDto<>(true, "수정 성공", resDto);

    }

    // 프로젝트 메인페이지에서 팀원 프로필 정보 불러오기용
    public UserResponseDto<?> OneUserInfo(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalInstantException("존재하지않는 사용자입니다.")
        );

        List<String> language = user.getLanguage().stream().map(Language::getLanguage).collect(Collectors.toList());

        ProfileResDto profileResDto = ProfileResDto.builder()
                .profileUrl(user.getProfileUrl())
                .role(user.getRole())
                .nickname(user.getNickname())
                .language(language)
                .github(user.getGithub())
                .figma(user.getFigma())
                .intro(user.getIntro())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();

        return new UserResponseDto<>(true, "유저 정보를 불러왔습니다.", profileResDto);
    }

}
