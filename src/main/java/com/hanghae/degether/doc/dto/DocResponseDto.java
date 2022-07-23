package com.hanghae.degether.doc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.hanghae.degether.doc.model.Doc;
import com.hanghae.degether.user.model.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocResponseDto {
    private Long id;
    private String title;
    private String content;
    private String docStatus;
    private String inCharge;
    private Boolean notice;
    private Boolean onGoing;
    private LocalDate startDate;
    private LocalDate endDate;
    private String nickname;
    private Long folderId;

    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private LocalDateTime createdDate;

    public DocResponseDto(Doc doc) {
        this.id = doc.getId();
        this.title = doc.getTitle();
        this.content = doc.getContent();
        this.docStatus = doc.getDocStatus();
        this.inCharge = doc.getInCharge().getNickName();
        this.notice = doc.getNotice();
        this.onGoing = doc.getOnGoing();
        this.startDate = doc.getStartDate();
        this.endDate = doc.getEndDate();
        this.nickname = doc.getUser().getNickName();
        this.createdDate = doc.getCreatedDate();
    }

    public DocResponseDto(Doc doc, User user) {
        this.id = doc.getId();
        this.title = doc.getTitle();
        this.docStatus = doc.getDocStatus();
        this.inCharge = user.getNickName();
        this.startDate = doc.getStartDate();
        this.endDate = doc.getEndDate();
    }

    public DocResponseDto(Long id, String title, String nickname, Long folderId) {
        this.id = id;
        this.title = title;
        this.nickname = nickname;
        this.folderId = folderId;
    }


}
