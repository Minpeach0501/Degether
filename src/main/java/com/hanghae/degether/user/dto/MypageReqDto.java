package com.hanghae.degether.user.dto;

import com.hanghae.degether.project.model.Language;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class MypageReqDto {
    private String profileUrl;
    private String role;
    private String nickname;
    private List<Language> languages;
    private String github;
    private String figma;
    private String intro;
    private String phoneNumber;
    private String email;
}
