package com.hanghae.degether.openvidu.repository;

import com.hanghae.degether.openvidu.model.Meetingnote;
import com.hanghae.degether.project.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingNoteRepository extends JpaRepository<Meetingnote, Long> {

    List<Meetingnote> findAllByProject(Project project);
}
