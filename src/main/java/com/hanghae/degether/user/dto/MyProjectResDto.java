package com.hanghae.degether.user.dto;

import com.hanghae.degether.project.model.Genre;
import com.hanghae.degether.project.model.Language;


import java.util.List;

public interface MyProjectResDto{
    Long getId();
    String getProjectName();
    String getProjectDescription();
    String getThumbnail();
    List<Language> getLanguage();
    List<Genre> getGenre();
    String getStep();

}
