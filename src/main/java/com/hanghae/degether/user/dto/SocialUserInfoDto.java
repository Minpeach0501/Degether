package com.hanghae.degether.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter

public class SocialUserInfoDto {
    private Long id;
    private String nickname;
    private String email;
    private String profileUrl;


    public SocialUserInfoDto(String id, String nickname, String profileUrl) {
        this.id = id;
        this.nickname = nickname;
        this.profileUrl = profileUrl;
    }
}
