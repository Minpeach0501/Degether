package com.hanghae.degether.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class ProfileResDto {


    private String profileUrl;
    private String role;
    private String nickname;
    private List<String> language;
    private String github;
    private String figma;
    private String intro;
    private String email;
    private String phoneNumber;

    @Builder
    public ProfileResDto(String profileUrl, String role, String nickname, List<String> language, String github, String figma, String intro, String email, String phoneNumber) {
        this.profileUrl = profileUrl;
        this.role = role;
        this.nickname = nickname;
        this.language = language;
        this.github = github;
        this.figma = figma;
        this.intro = intro;
        this.email =email;
        this.phoneNumber = phoneNumber;
    }
}
