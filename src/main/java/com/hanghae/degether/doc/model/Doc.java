package com.hanghae.degether.doc.model;


import com.hanghae.degether.doc.dto.DocRequestDto;
import com.hanghae.degether.doc.dto.StatusDto;
import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.project.model.Timestamped;
import com.hanghae.degether.user.model.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class Doc extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String docStatus;

    @Column(nullable = false)
    private Boolean notice = false;

    @Column(nullable = false)
    private Boolean onGoing = false;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private LocalDate endDate;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;


    @ManyToOne
    @JoinColumn(name = "incharge_id")
    private User inCharge;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;

    public Doc(Project project, DocRequestDto docRequestDto, User user, User user2, Folder folder) {
        this.title = docRequestDto.getTitle();
        this.content = docRequestDto.getContent();
        this.docStatus = docRequestDto.getDocStatus();
        this.notice = docRequestDto.getNotice();
        this.onGoing = docRequestDto.getOnGoing();
        this.startDate = docRequestDto.getStartDate();
        this.endDate = docRequestDto.getEndDate();
        this.user = user;
        this.project = project;
        this.inCharge = user2;
        this.folder = folder;
    }

    public void update(DocRequestDto docRequestDto, User user){
        this.title = docRequestDto.getTitle();
        this.content = docRequestDto.getContent();
        this.docStatus = docRequestDto.getDocStatus();
        this.notice = docRequestDto.getNotice();
        this.onGoing = docRequestDto.getOnGoing();
        this.startDate = docRequestDto.getStartDate();
        this.endDate = docRequestDto.getEndDate();
        this.inCharge = user;
    }

    public void update(StatusDto statusDto) {
        this.docStatus = statusDto.getDocStatus();
    }

    public void update(Folder folder) {
        this.folder = folder;
    }

}
