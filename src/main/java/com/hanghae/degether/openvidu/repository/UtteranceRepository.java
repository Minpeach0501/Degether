package com.hanghae.degether.openvidu.repository;

import com.hanghae.degether.openvidu.model.Meetingnote;
import com.hanghae.degether.openvidu.model.Utterance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UtteranceRepository extends JpaRepository<Utterance, Long> {
    List<Utterance> findAllByMeetingnote(Meetingnote meetingNote);
}
