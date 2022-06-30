package com.hanghae.degether.user.service;

import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.project.model.Zzim;
import com.hanghae.degether.project.repository.ProjectRepository;
import com.hanghae.degether.project.repository.UserProjectRepository;
import com.hanghae.degether.project.repository.ZzimRepository;
import com.hanghae.degether.user.dto.*;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.repository.UserRepository;
import com.hanghae.degether.user.security.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MypageService {

    private final ZzimRepository zzimRepository;

    private final UserProjectRepository userProjectRepository;

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    @Autowired
    public MypageService(ZzimRepository zzimRepository,
                         UserProjectRepository userProjectRepository,
                         ProjectRepository projectRepository,
                         UserRepository userRepository
    )
    {
        this.zzimRepository =zzimRepository;
        this.userProjectRepository =userProjectRepository;
        this.projectRepository =projectRepository;
        this.userRepository = userRepository;

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
}
