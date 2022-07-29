package com.hanghae.degether.openvidu.service;

import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
import com.hanghae.degether.openvidu.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class SttService {
    private String STT_TOKEN;

    @PostConstruct
    private void init() {
        getSttToken();
    }
    public void getSttToken(){
        System.out.println("getSttToken, " +STT_TOKEN + " -> ");
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        HttpStatus httpStatus = HttpStatus.CREATED;
        RestTemplate restTemplate = new RestTemplate();
        map.add("client_id", "dW1i2GgHkKl09IoVb1ug");
        map.add("client_secret", "cy_VJLiNQjCQ5Pw-9U43SX2QJF0NjzQuZy2wBIFH");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        String url = "https://openapi.vito.ai/v1/authenticate";
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        VitoResponseDto responseDto = restTemplate.postForObject(url, requestEntity, VitoResponseDto.class);
        STT_TOKEN = "bearer " + responseDto.getAccess_token();
        System.out.println(STT_TOKEN);
    }

    public String getSttId(String fileUrl, boolean resend) throws IOException {
        // resend 반복 토큰 요청 막기
        System.out.println("getSttId, token = " + STT_TOKEN);
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        HttpStatus httpStatus = HttpStatus.CREATED;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", STT_TOKEN);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        map.add("file", new MultipartInputStreamFileResource(new URL(fileUrl).openStream(), "send.mp4"));
        map.add("config", new VitoConfigDto());
        String url = "https://openapi.vito.ai/v1/transcribe";
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        VitoResponseDto vitoResponseDto = restTemplate.postForObject(url, requestEntity, VitoResponseDto.class);
        if (vitoResponseDto.getId() == null) {
            if ("H0002".equals(vitoResponseDto.getCode())) {
                //유효하지 않은 토큰
                if(resend){
                    System.out.println("resend");
                    getSttToken();
                    return getSttId(fileUrl,false);
                }else {
                    throw new CustomException(ErrorCode.VITO_H0002);
                }
            }else {
                throw new CustomException(ErrorCode.VITO_H0010);
            }

        }
        return vitoResponseDto.getId();
    }


    public VitoResponseDto getSttUtterance(String sttId, boolean resend){
        // resend 반복 토큰 요청 막기
        System.out.println("getSttUtterance, token = " + STT_TOKEN);
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        HttpStatus httpStatus = HttpStatus.CREATED;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", STT_TOKEN);
        System.out.println("getSttUtterance");

        String url = "https://openapi.vito.ai/v1/transcribe/"+sttId;
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        ResponseEntity<VitoResponseDto> vitoResponseDtoResponseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity , VitoResponseDto.class);
        VitoResponseDto vitoResponseDto = vitoResponseDtoResponseEntity.getBody();
        if ("H0002".equals(vitoResponseDto.getCode())) {
            //유효하지 않은 토큰
            if(resend){
                System.out.println("resend");
                getSttToken();
                return getSttUtterance(sttId,false);
            }else {
                throw new CustomException(ErrorCode.VITO_H0002);
            }
        }
        return vitoResponseDto;
    }
}
