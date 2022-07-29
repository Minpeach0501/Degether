package com.hanghae.degether.user.model;


import com.hanghae.degether.project.model.Language;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity(name = "UserInfo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long Id;

    @Column(nullable = false,unique = true)
    private  String username;

    @Column(nullable = false)
    private  String nickname;

    @Column(nullable = false)
    private  String password;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Language> language;

    @Column
    private  String profileUrl;

    @Column
    private  String role;

    @Column
    private  String github;

    @Column
    private String figma;

    @Column
    private String intro;

    @Column
    private String email;

    @Column
    private String phoneNumber;

    @Column
    private boolean status = true ;



    public User(String username, String nickname, String profileUrl, String password){
        this.username = username;
        this.nickname = nickname;
        this.profileUrl = profileUrl;
        this.password = password;
    }

    public void update(String profileUrl, String role, String nickname, List<Language> language, String github, String figma, String intro, String phoneNumber, String email) {
        this.profileUrl =profileUrl;
        this.role = role;
        this.nickname = nickname;
        this.language.clear();
        this.language.addAll(language);
        this.github = github;
        this.figma = figma;
        this.intro = intro;
        this.phoneNumber = phoneNumber;
        this.email = email;

    }


}
