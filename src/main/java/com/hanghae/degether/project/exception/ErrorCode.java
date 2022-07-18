package com.hanghae.degether.project.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    REQUIRED_LOGIN(401,"로그인이 필요합니다."),
    OVER_UPLOAD_SIZE(401,"최대 업로드 사이즈를 초과했습니다."),
    NOT_EXIST_PROJECT(401,"존재하지 않는 프로젝트 입니다."),
    NOT_EXIST_COMMENT(401,"존재하지 않는 댓글 입니다."),
    NOT_EXIST_USER(401,"로그인이 필요합니다."),
    UNAUTHORIZED(401,"로그인이 필요합니다."),
    DUPLICATED_APPLY(401,"로그인이 필요합니다."),
    NOT_APPLY(401,"로그인이 필요합니다."),
    DUPLICATED_JOIN(401,"로그인이 필요합니다.")
    ;
    private final int code;
    private final String message;
}
