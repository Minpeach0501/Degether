package com.hanghae.degether.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto<A> {
    private boolean ok;
    private String message;
    private A result;
    //private T results;

    public UserResponseDto(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }

//    public UserResponseDto(boolean ok, String message, T list) {
//        this.ok = ok;
//        this.message = message;
//        this.results = list;
//    }

    public UserResponseDto(boolean ok, String message, A result) {
        this.ok = ok;
        this.message = message;
        this.result = result;
    }
}
