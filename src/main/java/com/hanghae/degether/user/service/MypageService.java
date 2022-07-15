package com.hanghae.degether.user.service;

import com.hanghae.degether.project.model.Language;
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

        // 내가 참여한 모든 프로 젝트들 불러오기 projection 사용
        List<MyProjectResDto> myproject = userProjectRepository.findAllByUserAndIsTeam(user, true);

        ResultDto resultDto = ResultDto.builder()
                .profileUrl(mypageReqDto.getProfileUrl())
                .role(mypageReqDto.getRole())
                .nickname(mypageReqDto.getNickname())
                .language(mypageReqDto.getLanguage())
                .github(mypageReqDto.getGithub())
                .figma(mypageReqDto.getFigma())
                .intro(mypageReqDto.getIntro())
                .zzim(Zzim)
                .myProject(myproject)
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
        String profileUrl = "";
        s3Uploader.deleteFromS3(profileUrl);

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalInstantException("등록되지 않은 사용자입니다.")
        );

        if (!file.isEmpty()) {
            //이미지 업로드
            profileUrl = s3Uploader.upload(file, reqDto.getProfileUrl());
        }

        List<Language> language = reqDto.getLanguage().stream().map((string) -> Language.builder().language(string).build()).collect(Collectors.toList());
        String nickname = reqDto.getNickname();
        String intro = reqDto.getIntro();

        int nicknameL = nickname.length();
        int introL = intro.length();

        if (nicknameL > 10) {
            throw new IllegalArgumentException("글자수가 초과되었습니다.");
        }
        if (nicknameL < 2) {
            throw new IllegalArgumentException("글자수가 부족합니다.");
        }
        if (introL > 20) {
            throw new IllegalArgumentException("글자수가 초과되었습니다.");
        }


        LoginResDto resDto = LoginResDto.builder()
                .profileUrl(reqDto.getProfileUrl())
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
                .build();
        return new UserResponseDto<>(true, "유저 정보를 불러왔습니다.", profileResDto);
    }

}
