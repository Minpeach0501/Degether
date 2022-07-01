package com.hanghae.degether.doc.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DocRequestDto {

    private String title;
    private String content;
    private String docStatus;
    private Long inCharge;
    private Boolean notice = false;
    private Boolean onGoing = false;
    private LocalDate startDate;
    private LocalDate endDate;

}
