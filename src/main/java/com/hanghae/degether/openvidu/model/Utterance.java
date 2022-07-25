package com.hanghae.degether.openvidu.model;

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
public class Utterance {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    @Column
    private Long start_at;
    @Column
    private Long duration;
    @Column
    private String msg;
    @Column
    private int spk;
    @ManyToOne
    @JoinColumn(name = "meetingnote_id")
    private Meetingnote meetingnote;
}
