package com.hanghae.degether.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter@Setter@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {
    private boolean ok;
    private String message;
    private T result;
    private T results;

    private Long projectId;
}
