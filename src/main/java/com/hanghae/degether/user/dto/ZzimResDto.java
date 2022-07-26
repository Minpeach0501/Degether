package com.hanghae.degether.user.dto;

import com.hanghae.degether.project.model.Genre;
import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.project.model.Zzim;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ZzimResDto {
   private Long projectId;
    private String projectName;
    private String projectDescription;
    private String thumbnail;
    private List<String> language;
    private List<String> genre;
    private String step;

    public ZzimResDto(Zzim zzim) {

     this.projectId = zzim.getProject().getId();
     this.projectName = zzim.getProject().getProjectName();
     this.projectDescription = zzim.getProject().getProjectDescription();
     this.thumbnail = zzim.getProject().getThumbnail();
     this.language = zzim.getProject().getLanguages().stream().map(Language::getLanguage).collect(Collectors.toList());
     this.genre = zzim.getProject().getGenres().stream().map(Genre::getGenre).collect(Collectors.toList());
     this.step = zzim.getProject().getStep();
    }
}
