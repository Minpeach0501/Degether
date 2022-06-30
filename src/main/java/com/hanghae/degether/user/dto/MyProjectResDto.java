package com.hanghae.degether.user.dto;

import com.hanghae.degether.project.model.Genre;
import com.hanghae.degether.project.model.Language;


import java.util.List;

public interface MyProjectResDto{
    Long projectId();
    String projectName();
    String projectDescription();
    String thumbnail();
    List<Language> language();
    List<Genre> genre();
    String step();
}
