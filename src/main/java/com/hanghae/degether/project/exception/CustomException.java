package com.hanghae.degether.project.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{
    private int code;
    public CustomException(int code, String message){
        super(message);
        this.code = code;
    }
}
