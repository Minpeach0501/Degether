package com.hanghae.degether.project.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Genre {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    @Column
    private String genre;
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
}
