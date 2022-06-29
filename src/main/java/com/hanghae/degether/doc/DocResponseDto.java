package com.hanghae.degether.doc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hanghae.degether.User.User;
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

    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private LocalDateTime createdDate;

    public DocResponseDto(Doc doc) {
        this.id = doc.getId();
        this.title = doc.getTitle();
        this.content = doc.getContent();
        this.docStatus = doc.getDocStatus();
        this.inCharge = doc.getInCharge().getNickname();
        this.notice = doc.getNotice();
        this.onGoing = doc.getOnGoing();
        this.startDate = doc.getStartDate();
        this.endDate = doc.getEndDate();
        this.nickname = doc.getUser().getNickname();
        this.createdDate = doc.getCreatedDate();
    }

    public DocResponseDto(Doc doc, User user) {
        this.id = doc.getId();
        this.title = doc.getTitle();
        this.docStatus = doc.getDocStatus();
        this.inCharge = user.getNickname();
        this.startDate = doc.getStartDate();
        this.endDate = doc.getEndDate();
    }

    public DocResponseDto(Long id, String title, String nickname) {
        this.id = id;
        this.title = title;
        this.nickname = nickname;
    }


}
