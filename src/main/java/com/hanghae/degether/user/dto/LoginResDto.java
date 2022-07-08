package com.hanghae.degether.user.dto;

import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LoginResDto {
    private Long userId;
    private String username ;
    private String  profileUrl;
    private String role;
    private String  nickname;
    private List<String> language;
    private String  github;
    private String  figma;
    private String  intro;
    private String phonenumber;
    private String email ;

    public LoginResDto(Optional<User> user) {
        this.userId = user.get().getId();
        this.username = user.get().getUsername();
        this.profileUrl = user.get().getProfileUrl();
        this.role = user.get().getRole();
        this.nickname = user.get().getNickname();
        this.language = user.get().getLanguage().stream().map(Language::getLanguage).collect(Collectors.toList());
        this.github = user.get().getGithub();
        this.figma = user.get().getFigma();
        this.intro = user.get().getIntro();
        this.phonenumber = user.get().getPhoneNumber();
        this.email = user.get().getEmail();
    }
}
