package com.hanghae.degether.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hanghae.degether.project.model.Genre;
import com.hanghae.degether.project.model.Language;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public class ProjectDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request{
        @NotEmpty(message = "프로젝트명을 입력해 주세요.")
        @Size(min = 2, max = 20, message = "2글자 이상, 20글자 이하로 입력해 주세요.")
        private String projectName;

        @NotEmpty(message = "프로젝트 설명을 입력해 주세요.")
        @Size(min = 2, max = 50, message = "2글자 이상, 50글자 이하로 입력해 주세요.")
        private String projectDescription;

        @NotNull(message = "올바른 값을 입력해 주세요.")
        @PositiveOrZero(message = "0 이상의 숫자를 입력해 주세요.")
        private Integer feCount;

        @NotNull(message = "올바른 값을 입력해 주세요.")
        @PositiveOrZero(message = "0 이상의 숫자를 입력해 주세요.")
        private Integer beCount;

        @NotNull(message = "올바른 값을 입력해 주세요.")
        @PositiveOrZero(message = "0 이상의 숫자를 입력해 주세요.")
        private Integer deCount;

        private String github;

        private String figma;

        private LocalDate deadLine;

        private String step;
        private List<String> language;
        private List<String> genre;
    }
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response{
        private Long projectId;
        private String thumbnail;
        private String projectName;
        private String projectDescription;
        private Integer feCount;
        private Integer beCount;
        private Integer devCount;
        private Integer deCount;
        private Integer feCurrentCount;
        private Integer beCurrentCount;
        private Integer deCurrentCount;
        private String github;
        private String figma;
        private LocalDate deadLine;
        @JsonProperty("dDay")
        private Long dDay;
        private String step;
        private List<String> language;
        private String languageString;
        private List<String> genre;
        private List<ProjectDto.File> infoFiles;
        private List<CommentDto.Response> comment;
        private List<UserDto> user;
        private List<UserDto> applyUser;
        private List<DocDto> notice;
        private List<DocDto> todo;
        private Boolean isZzim;
        private Long zzimCount;
    }
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Slice{
        private Boolean isLast;
        private List<ProjectDto.Response> list;
        private List<ProjectDto.Response> myProject;
    }
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class File{
        private String fileName;
        private String fileUrl;
    }


}
