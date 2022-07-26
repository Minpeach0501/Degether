package com.hanghae.degether.project.model;

import com.hanghae.degether.user.model.User;
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
public class Language {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    @Column
    private String language;
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;
}
