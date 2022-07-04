package com.hanghae.degether.user.dto;

import com.hanghae.degether.project.model.Genre;
import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.project.model.Zzim;

import java.util.List;

public class ZzimResDto {
   private Long projectId;
    private String projectName;
    private String projectDescription;
    private String thumbnail;
    private List<Language> language;
    private List<Genre> genre;
    private String step;

    public ZzimResDto(Zzim zzim) {

     this.projectId = zzim.getProject().getId();
     this.projectName = zzim.getProject().getProjectName();
     this.projectDescription = zzim.getProject().getProjectDescription();
     this.thumbnail = zzim.getProject().getThumbnail();
     this.language = zzim.getProject().getLanguages();
     this.genre = zzim.getProject().getGenres();
     this.step = zzim.getProject().getStep();
    }
}
