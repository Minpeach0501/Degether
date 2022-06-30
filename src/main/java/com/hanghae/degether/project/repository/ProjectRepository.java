package com.hanghae.degether.project.repository;

import com.hanghae.degether.project.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {



    // @Query("select p.id from Project as p left join project_genre as pg on p.id = pg.project_id where pg.genre = :genre ")
    // public List<?> findAllBySearchQuery(String search, String language, String genre, String step);
}
