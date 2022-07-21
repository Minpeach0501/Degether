package com.hanghae.degether.openvidu.model;

import javax.persistence.*;

@Entity
public class MeetingNote {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    @Column(unique = true, nullable = false)
    private String sttId;
    @Column
    private Long createdAt;
    @Column
    private Long duration;
    @Column
    private String url;
    @Column
    private Boolean status;


}
