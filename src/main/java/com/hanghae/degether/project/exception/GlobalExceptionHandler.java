package com.hanghae.degether.project.exception;

import com.hanghae.degether.project.dto.ResponseDto;
import com.nimbusds.oauth2.sdk.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseDto<?> illegalArgumentException(IllegalArgumentException e){
        log.error("IllegalArgumentException",e);
        return ResponseDto.builder()
                .ok(false)
                .message(e.getMessage())
                .build();
    }    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDto<?> methodArgumentNotValidException(MethodArgumentNotValidException e){
        log.error("IllegalArgumentException",e);
        return ResponseDto.builder()
                .ok(false)
                .message(e.getBindingResult().getAllErrors().get(0).getDefaultMessage())
                .build();
    }

}