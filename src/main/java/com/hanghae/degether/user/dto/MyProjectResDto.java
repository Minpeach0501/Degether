package com.hanghae.degether.user.dto;

import com.hanghae.degether.project.model.Genre;
import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.project.model.Project;
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

    Long Id;

    String projectName;

    String description;

    String Thumbnail;

    List<String> language;

    List<String> genre;

    String Step;

    Integer devCount;
    Integer deCount;



    public MyProjectResDto(UserProject userProject) {
        Project project = userProject.getProject();
        int devCount = 0;
        int deCount = 0;
        for (UserProject userProject2 : project.getUserProjects()) {
            String role = userProject2.getUser().getRole();
            if ("백엔드 개발자".equals(role) || "프론트엔드 개발자".equals(role)) {
                devCount++;
            } else if ("디자이너".equals(role)) {
                deCount++;
            }
        }
        this.Id = userProject.getProject().getId();
        this.projectName = userProject.getProject().getProjectName();
        this.description = userProject.getProject().getProjectDescription();
        this.Thumbnail = userProject.getProject().getThumbnail();
        this.language = userProject.getProject().getLanguages().stream().map(Language::getLanguage).collect(Collectors.toList());
        this.genre = userProject.getProject().getGenres().stream().map(Genre::getGenre).collect(Collectors.toList());
        this.Step = userProject.getProject().getStep();
        this.devCount = devCount;
        this.deCount = deCount;
    }
}
