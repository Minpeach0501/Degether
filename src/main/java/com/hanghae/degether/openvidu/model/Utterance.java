package com.hanghae.degether.openvidu.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Utterance {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
}
