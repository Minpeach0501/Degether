package com.hanghae.degether.doc;


import com.hanghae.degether.project.dto.DocDto;
import com.hanghae.degether.project.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface DocRepository extends JpaRepository<Doc, Long> {
    List<Doc> findAllByProjectOrderByCreatedDateDesc(Project project);

    List<Doc> findAllByProjectAndNoticeOrderByCreatedDateDesc(Project project, boolean notice);

    List<Doc> findAllByProjectAndOnGoingOrderByCreatedDateDesc(Project project, boolean onGoing);
}
