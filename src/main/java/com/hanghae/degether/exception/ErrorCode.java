package com.hanghae.degether.exception;

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
    DUPLICATED_JOIN(418,"이미 가입된 유저 입니다."),
    UNAUTHORIZED_TOKEN(419,"유효하지 않은 토큰 입니다."),
    EXPIRED_TOKEN(420,"유효기간이 만료된 토큰입니다.."),
    SIGNATURE_TOKEN(421,"기존 서명을 확인할 수 없습니다."),
    UNSUPPORT_TOKEN(422,"지원하지 않는 토큰입니다."),
    MALFORMED_TOKEN(423,"토큰형식이 맞지 않습니다."),
    DELETED_USER(424,"탈퇴한 회원입니다."),
    NAVER_TOKEN(425,"네이버 오류입니다."),
    VITO_H0002(426,"STT 토큰 오류 입니다." ),
    VITO_H0010(427,"STT 서비스 오류 입니다."),
    NOT_EXIST_MEETING_NOTE(428,"존재하지 않는 프로젝트 입니다."),
    OPENVIDU_ERROR(429,"화상채팅 서버 오류 입니다."),
    MANY_PROJECT(430,"참가 할 수 있는 프로젝트는 총 3개 입니다."),
    FAILED_MESSAGE(431, "메세지 보내기에 실패했습니다."),
    APPLY_USER_MANY_PROJECT(432,"지원자의 참가 가능 프로젝트 수 초과 입니다.")

    ;
    private final int code;
    private final String message;
}
