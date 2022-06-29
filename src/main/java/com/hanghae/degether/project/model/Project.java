package com.hanghae.degether.project.model;

import com.hanghae.degether.project.dto.ProjectResponseDto;
import com.hanghae.degether.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project extends Timestamped {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    @Column
    private String thumbnail;
    @Column
    private String projectName;
    @Column
    private String projectDescription;
    @Column
    private int feCount;
    @Column
    private int beCount;
    @Column
    private int deCount;
    @Column
    private String github;
    @Column
    private String figma;
    @Column
    private LocalDate deadLine;
    @Column
    private String step;
    @OneToMany(fetch = FetchType.LAZY)
    private List<Language> language;
    @OneToMany(fetch = FetchType.LAZY)
    private List<Genre> genre;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @OneToMany
    @JoinColumn(name = "project_id")
    private List<Comment> comments;

    public ProjectResponseDto.Get update(
                                            String projectName,
                                            String projectDescription,
                                            int feCount,
                                            int beCount,
                                            int deCount,
                                            String github,
                                            String figma,
                                            LocalDate deadLine,
                                            String step,
                                            List<Language> language,
                                            List<Genre> genre,
                                            String thumbnail) {
            this.projectName = projectName;
            this.projectDescription = projectDescription;
            this.feCount = feCount;
            this.beCount = beCount;
            this.deCount = deCount;
            this.github = github;
            this.figma = figma;
            this.deadLine = deadLine;
            this.step = step;
            this.language = language;
            this.genre = genre;
            this.thumbnail = thumbnail;
            return ProjectResponseDto.Get.builder()
                    .projectName(projectName)
                    .projectDescription(projectDescription)
                    .feCount(feCount)
                    .beCount(beCount)
                    .deCount(deCount)
                    .github(github)
                    .figma(figma)
                    .deadLine(deadLine)
                    .step(step)
                    .language(language)
                    .genre(genre)
                    .thumbnail(thumbnail)
                    .build();
    }
}
