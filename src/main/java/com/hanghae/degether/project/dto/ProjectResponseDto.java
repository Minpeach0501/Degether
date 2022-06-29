package com.hanghae.degether.project.dto;

import com.hanghae.degether.project.model.Genre;
import com.hanghae.degether.project.model.Language;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProjectResponseDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
        private List<Language> language;
        private List<Genre> genre;
    }
}
