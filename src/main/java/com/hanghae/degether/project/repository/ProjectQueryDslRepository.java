package com.hanghae.degether.project.repository;

import com.hanghae.degether.project.model.Project;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.hanghae.degether.project.model.QGenre.genre1;
import static com.hanghae.degether.project.model.QLanguage.language1;
import static com.hanghae.degether.project.model.QProject.project;
import static org.apache.logging.log4j.util.Strings.isEmpty;

@Repository
@RequiredArgsConstructor
public class ProjectQueryDslRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public Slice<Project> getProjectsBySearch(String search, String language, String genre, String step, Pageable pageable){
        //TODO: 성능 개선 필요
        List<Project> content = jpaQueryFactory
                // .select(Projections.bean(ProjectDto.Response.class,
                //         project.thumbnail,
                //         project.projectName,
                //         project.projectDescription,
                //         project.feCount,
                //         project.beCount,
                //         project.deCount,
                //         project.github,
                //         project.figma,
                //         project.deadLine,
                //         project.step,
                //         //TODO: Entity가 아닌 ElementCollection select 방법 찾기
                //         project.languages,
                //         project.genres
                //     )
                // )
                // .from(project)
                .selectFrom(project)
                .leftJoin(project.languages, language1)
                .leftJoin(project.genres, genre1)
                .where(
                        searchContains(search),
                        stepEq(step),
                        languageContains(language),
                        genreContains(genre)
                )
                .groupBy(project.id)
                .orderBy(projectSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }
        return new SliceImpl<>(content, pageable, hasNext);
    }
    private OrderSpecifier<?> projectSort(Pageable pageable) {
        if (!pageable.getSort().isEmpty()) {
            for (Sort.Order order : pageable.getSort()) {
                Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
                switch (order.getProperty()){
                    case "createdDate":
                        return new OrderSpecifier<>(direction, project.createdDate);
                    case "content":
                        return new OrderSpecifier<>(direction, project.deadLine);
                }
            }
        }
        return null;
    }

    private Predicate searchContains(String search) {
        return isEmpty(search) ? null : project.projectName.contains(search);
    }
    private Predicate languageContains(String language) {
        return isEmpty(language) ? null : language1.language.eq(language);
    }
    private Predicate genreContains(String genre) {
        return isEmpty(genre) ? null : genre1.genre.eq(genre);
    }
    private Predicate stepEq(String step) {
        return isEmpty(step) ? null : project.step.eq(step);
    }

}
