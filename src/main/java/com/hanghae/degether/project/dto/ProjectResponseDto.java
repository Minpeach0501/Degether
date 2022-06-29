package com.hanghae.degether.project.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProjectResponseDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Get{
        private String thumbnail;
        private String projectName;
        private String projectDescription;
        private int feCount;
        private int beCount;
        private int deCount;
        private String github;
        private String figma;
        private LocalDate deadLine;
        private String step;
        private List<String> language;
        private List<String> genre;
    }
}
