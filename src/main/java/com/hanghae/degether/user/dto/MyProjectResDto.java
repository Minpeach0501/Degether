package com.hanghae.degether.user.dto;

import com.hanghae.degether.project.model.Genre;
import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.project.model.UserProject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyProjectResDto{

    private Long Id;

    private String projectName;

    private String description;

    private String Thumbnail;

    private List<String> language;

    private List<String> genre;

    private String Step;



    public MyProjectResDto(UserProject userProject) {
        this.Id = userProject.getProject().getId();
        this.projectName = userProject.getProject().getProjectName();
        this.description = userProject.getProject().getProjectDescription();
        this.Thumbnail = userProject.getProject().getThumbnail();
        this.language = userProject.getProject().getLanguages().stream().map(Language::getLanguage).collect(Collectors.toList());
        this.genre = userProject.getProject().getGenres().stream().map(Genre::getGenre).collect(Collectors.toList());
        this.Step = userProject.getProject().getStep();
    }
}
