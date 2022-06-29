package com.hanghae.degether.project.repository;

import com.hanghae.degether.project.dto.ProjectDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.hanghae.degether.project.model.QProject.project;
import static com.hanghae.degether.project.model.QLanguage.language1;
import static com.hanghae.degether.project.model.QGenre.genre1;
import static org.apache.logging.log4j.util.Strings.isEmpty;

@Repository
@RequiredArgsConstructor
public class ProjectQueryDslRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public List<ProjectDto.Response> getProjectsBySearch(String search, String language, String genre, String step){
        //TODO: 성능 개선 필요
        return jpaQueryFactory
                        .select(Projections.bean(ProjectDto.Response.class,
                                project.thumbnail,
                                project.projectName,
                                project.projectDescription,
                                project.feCount,
                                project.beCount,
                                project.deCount,
                                project.github,
                                project.figma,
                                project.deadLine,
                                project.step,
                                //TODO: Entity가 아닌 ElementCollection select 방법 찾기
                                project.language,
                                project.genre
                            )
                        )
                        .from(project)
                .leftJoin(project.language,language1)
                .on(languageContains(language))
                .leftJoin(project.genre,genre1)
                .on(genreContains(genre))
                        .where(
                                searchContains(search),
                                stepEq(step)
                        )
                .fetch();
    }


    private Predicate searchContains(String search) {
        return isEmpty(search) ? null : project.projectName.contains(search);
    }
    private Predicate languageContains(String language) {
        return isEmpty(language) ? null : language1.language.eq(language);
    }
    private Predicate genreContains(String genre) {
        return isEmpty(genre) ? null : genre1.genre.contains(genre);
    }
    private Predicate stepEq(String step) {
        return isEmpty(step) ? null : project.step.eq(step);
    }

}
