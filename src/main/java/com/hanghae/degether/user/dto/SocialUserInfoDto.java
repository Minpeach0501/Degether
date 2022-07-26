package com.hanghae.degether.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter

public class SocialUserInfoDto {
    private String id;
    private String nickName;
    private String email;
    private String profileUrl;


    public SocialUserInfoDto(String id, String nickname, String profileUrl) {
        this.id = id;
        this.nickName = nickname;
        this.profileUrl = profileUrl;
    }


}
