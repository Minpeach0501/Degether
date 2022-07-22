package com.hanghae.degether.openvidu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VitoResponseDto {
    private String access_token;
    private Integer expire_at;
    private String code;
    private String id;
    private String errorcode;
    private String status;
    private Results results;

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Results{
        private List<Utterance> utterances;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Utterance{
        private Long start_at;
        private Long duration;
        private String msg;
        private int spk;
    }
}
