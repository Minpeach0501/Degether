package com.hanghae.degether.exception;

import com.hanghae.degether.project.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
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
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> customException(CustomException e){
        log.error("CustomException",e);
        return ResponseEntity.status(e.getCode())
                .body(e.getMessage());
        // return ResponseDto.builder()
        //         .ok(false)
        //         .message(e.getMessage())
        //         .build();
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValidException(MethodArgumentNotValidException e){
        log.error("MethodArgumentNotValidException",e);
        return ResponseEntity.status(450)
                .body("에러");
        // return ResponseDto.builder()
        //         .ok(false)
        //         .message(e.getBindingResult().getAllErrors().get(0).getDefaultMessage())
        //         .build();
    }

//    @ExceptionHandler(NullPointerException.class)
//    public ResponseDto<?> NullPointerException(NullPointerException e){
//        log.error("NullPointerException",e);
//        return ResponseDto.builder()
//                .ok(false)
//                .message(e.getMessage())
//                .build();
//    }


}