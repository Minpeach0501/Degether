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
    private List<String> language= null;
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
}
