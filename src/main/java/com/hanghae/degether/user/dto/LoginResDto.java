package com.hanghae.degether.user.dto;

import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.user.model.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
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
    private String phoneNumber;
    private String email ;

    public LoginResDto(Optional<User> user) {
        this.userId = user.get().getId();
        this.username = user.get().getUsername();
        this.profileUrl = user.get().getProfileUrl();
        this.role = user.get().getRole();
        this.nickname = user.get().getNickname();
        if(user.get().getLanguage() != null){
            this.language =user.get().getLanguage().stream().map(Language::getLanguage).collect(Collectors.toList());
        } else {
            this.language = null;
        }
        this.github = user.get().getGithub();
        this.figma = user.get().getFigma();
        this.intro = user.get().getIntro();
        this.phoneNumber = user.get().getPhoneNumber();
        this.email = user.get().getEmail();
    }
    //MyUpdateDto 를 만들어서 할필요가 없기에 생성자를 추가해 dto 파일 하나를 줄임
    public LoginResDto(String profileUrl, String role, String nickname, List<String> language, String github, String figma, String intro, String phoneNumber, String email) {
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
