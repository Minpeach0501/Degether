package com.hanghae.degether.user.dto;

import com.hanghae.degether.project.model.Language;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MyUpdateDto {

    private String profileUrl;
    private String role;
    private String nickname;
    private List<Language> language;
    private String github;
    private String figma;
    private String intro;
    private String phoneNumber;
    private String email;

    public MyUpdateDto(String profileUrl, String role, String nickname, List<Language> language, String github, String figma, String intro, String phoneNumber, String email) {
        this.profileUrl = profileUrl;
        this.role = role;
        this.nickname = nickname;
        this.language = language;
        this.github = github;
        this.figma = figma;
        this.intro = intro;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }
}
