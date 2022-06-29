package com.hanghae.degether.project.model;

import com.hanghae.degether.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends Timestamped {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column
    private String comment;
}
