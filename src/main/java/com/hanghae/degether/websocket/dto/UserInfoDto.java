package com.hanghae.degether.websocket.dto;


import com.hanghae.degether.user.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserInfoDto {

    private String username;
    private String password;
    private String nickName;
    private String profileUrl;
    private String kakaoId;
    private String googleId;
    private String naverId;
    private String intro;

    // 유저로 값을 찾아와서 채팅시 유저정보를 넣어주기 위함
    public UserInfoDto(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.nickName = user.getNickName();
        this.profileUrl = user.getProfileUrl();
        this.intro = user.getIntro();
    }
}
