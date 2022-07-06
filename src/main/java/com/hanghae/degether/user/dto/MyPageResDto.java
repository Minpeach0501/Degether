package com.hanghae.degether.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyPageResDto {

    private boolean ok;

    private String message;

    private ResultDto result;

    private MyUpdateDto updateDto;

    public MyPageResDto(boolean ok, String message, ResultDto result) {
        this.ok = ok;
        this.message = message;
        this.result =  result;
    }
    public MyPageResDto(boolean ok, String message, MyUpdateDto updateDto) {
        this.ok = ok;
        this.message = message;
        this.updateDto =  updateDto;
    }
}
