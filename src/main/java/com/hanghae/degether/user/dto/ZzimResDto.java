package com.hanghae.degether.user.dto;

import com.hanghae.degether.project.model.*;
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

    Integer devCount;
    Integer deCount;

    public ZzimResDto(Zzim zzim) {

     Project project = zzim.getProject();
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
     this.projectId = zzim.getProject().getId();
     this.projectName = zzim.getProject().getProjectName();
     this.projectDescription = zzim.getProject().getProjectDescription();
     this.thumbnail = zzim.getProject().getThumbnail();
     this.language = zzim.getProject().getLanguages().stream().map(Language::getLanguage).collect(Collectors.toList());
     this.genre = zzim.getProject().getGenres().stream().map(Genre::getGenre).collect(Collectors.toList());
     this.step = zzim.getProject().getStep();
     this.devCount = devCount;
     this.deCount = deCount;
    }
}
