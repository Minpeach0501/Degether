package com.hanghae.degether.openvidu.model;

import com.hanghae.degether.project.model.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meetingnote {
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
    @OneToMany(mappedBy = "meetingnote", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Utterance> utterances;
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    public void updateUtterances(List<Utterance> utterances) {
        this.utterances.addAll(utterances);
        this.status = true;
    }
}
