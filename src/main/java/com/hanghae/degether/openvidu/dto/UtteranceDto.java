package com.hanghae.degether.openvidu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

public class UtteranceDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response{
        private Long id;
        private Long start_at;
        private Long duration;
        private String msg;
    }
}
