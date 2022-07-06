package com.hanghae.degether.user.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "UserInfo")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long Id;

    @Column
    private  String username;

    @Column(nullable = false)
    private  String nickname;

    @Column(nullable = false)
    private  String password;

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


    public User(String username, String nickname, String profileUrl, String password) {
        this.username = username;
        this.nickname = nickname;
        this.profileUrl = profileUrl;
        this.password = password;
    }
}