package com.hanghae.degether.user.dto;

import lombok.*;

import javax.validation.constraints.*;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MypageReqDto {
    private String profileUrl;

    @NotEmpty(message = "역할을 입력해 주세요.")
    private String role;

    @NotEmpty(message = "닉네임을 입력해 주세요.")
    @Size(min = 2, max = 10, message = "2글자 이상, 10글자 이하로 입력해 주세요.")
    private String nickname;

    private List<String> language;

    @NotEmpty(message = "깃허브주소를 입력해 주세요.")
    private String github;

    @NotEmpty(message = "피그마주소를 입력해 주세요.")
    private String figma;

    @NotEmpty(message = "한줄소개를 입력해 주세요.")
    @Size(min = 2, max = 10, message = "2글자 이상, 10글자 이하로 입력해 주세요.")
    private String intro;

    @NotEmpty(message = "전화번호를 입력해 주세요.")
    @Pattern
            (regexp = "^01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$",
            message = "전화번호 양식에 맞지않습니다.")
    private String phoneNumber;

    @NotEmpty(message = "이메일을 입력해 주세요.")
    @NotNull(message = "이메일을 입력해 주세요.")
    @Email
    private String email;
}
