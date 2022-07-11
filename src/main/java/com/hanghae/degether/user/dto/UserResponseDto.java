package com.hanghae.degether.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto<T> {
    private boolean ok;
    private String message;
    private T result;


    public UserResponseDto(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }


    public UserResponseDto(boolean ok, String message, T result) {
        this.ok = ok;
        this.message = message;
        this.result = result;
    }
}
