package com.hanghae.degether.project.model;

import com.hanghae.degether.user.model.User;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProject {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column
    private boolean isTeam;

    public void changeIsTeam(boolean isTeam) {
        this.isTeam = isTeam;
    }
}
