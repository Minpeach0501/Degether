package com.hanghae.degether.project.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocDto {
    private Long docId;
    private String title;
    private String nickname;
    private String inCharge;
    private String docStatus;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
