package com.hanghae.degether.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class SocialUserInfoDto {
    private Long id;
    private String nickname;
    private String email;
    private String profileUrl;

    public SocialUserInfoDto(String nickname, String email, String profileUrl) {
        this.nickname = nickname;
        this.email = email;
        this.profileUrl = profileUrl;
    }

    public SocialUserInfoDto(Long id, String nickname, String email, String profileUrl) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.profileUrl = profileUrl;
    }
}
