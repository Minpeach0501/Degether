package com.hanghae.degether.doc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {
    private boolean ok;
    private String message;
    private T result;
    //private T results;

    public ResponseDto(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }

//    public ResponseDto(boolean ok, String message, T list) {
//        this.ok = ok;
//        this.message = message;
//        this.results = list;
//    }

    public ResponseDto(boolean ok, String message, T t) {
        this.ok = ok;
        this.message = message;
        this.result = t;
    }
}
