package com.hanghae.degether.user.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MypageReqDto {
    private String profileUrl;
    private String role;
    private String nickname;
    private List<String> language;
    private String github;
    private String figma;
    private String intro;
    private String phoneNumber;
    private String email;
}
