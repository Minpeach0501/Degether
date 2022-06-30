package com.hanghae.degether.user.dto;

import com.hanghae.degether.project.model.Language;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ResultDto {
    private String profileUrl;
    private String role;
    private String nickname ;
    private List<Language> language;
    private String github;
    private String figma;
    private String intro;

    private List<ZzimResDto> zzim;

    private List<MyProjectResDto> myProject;

    public ResultDto(String profileUrl, String role, String nickname, List<Language> languages, String github, String figma, String intro, List<ZzimResDto> zzim, List<MyProjectResDto> myproject) {
        this.profileUrl =profileUrl;
        this.role= role;
        this.nickname = nickname;
        this.language = languages;
        this.github = github;
        this.figma = figma;
        this.intro = intro;
        this.zzim = zzim;
        this.myProject = myproject;
    }
}
