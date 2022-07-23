package com.hanghae.degether.openvidu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

public class MeetingNoteDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response{
        private Long id;
        private Long createdAt;
        private String title;
        private Long duration;
        private String url;
        private Boolean status;
        // private List<UtteranceDto> utterances;
    }
}
