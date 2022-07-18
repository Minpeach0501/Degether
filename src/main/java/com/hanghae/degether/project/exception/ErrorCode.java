package com.hanghae.degether.project.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    REQUIRED_LOGIN(410,"로그인이 필요합니다."),
    OVER_UPLOAD_SIZE(411,"최대 업로드 사이즈를 초과했습니다."),
    NOT_EXIST_PROJECT(412,"존재하지 않는 프로젝트 입니다."),
    NOT_EXIST_COMMENT(413,"존재하지 않는 댓글 입니다."),
    NOT_EXIST_USER(414,"존재하지 않는 유저 입니다."),
    UNAUTHORIZED(415,"권한이 없습니다."),
    DUPLICATED_APPLY(416,"이미 지원한 프로젝트 입니다."),
    NOT_APPLY(417,"해당 유저는 지원한 상태가 아닙니다."),
    DUPLICATED_JOIN(418,"이미 가입된 유저 입니다.")
    ;
    private final int code;
    private final String message;
}
