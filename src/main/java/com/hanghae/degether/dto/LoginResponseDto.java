package com.hanghae.degether.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponseDto {
    private boolean ok ;
    private String message;

    public LoginResponseDto(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }
}
