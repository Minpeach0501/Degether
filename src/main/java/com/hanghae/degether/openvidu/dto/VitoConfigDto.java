package com.hanghae.degether.openvidu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
//VITO api 호출시 설정
public class VitoConfigDto {
    private Diarization diarization = new Diarization();
    @JsonProperty("use_multi_channel?")
    private boolean use_multi_channel = false;
    @JsonProperty("use_itn?")
    private boolean use_itn = true;
    @JsonProperty("use_disfluency_filter?")
    private boolean use_disfluency_filter = true;
    @JsonProperty("use_profanity_filter?")
    private boolean use_profanity_filter;
    @JsonProperty("paragraph_splitter?")
    private ParagraphSplitter paragraph_splitter = new ParagraphSplitter();
    @Getter
    private static class Diarization{
        @JsonProperty("use_ars?")
        private boolean use_ars = false;
        @JsonProperty("use_verification?")
        private boolean use_verification = false;
        @JsonProperty("user_id?")
        private String user_id;
        @JsonProperty("partner_id?")
        private String partner_id;
    }
    @Getter
    private static class ParagraphSplitter{
        private Integer min = 10;
        private Integer max = 50;
    }
}
