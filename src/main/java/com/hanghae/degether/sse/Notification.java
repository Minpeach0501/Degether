package com.hanghae.degether.sse;

import com.hanghae.degether.project.model.Timestamped;
import com.hanghae.degether.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String content;
    //알림 내용 - 50자 이내
    @Column(nullable = false)
    private Boolean isRead;
    //읽었는지에 대한 여부


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User receiver;
}