package com.hanghae.degether.doc.repository;



import com.hanghae.degether.doc.model.Doc;
import com.hanghae.degether.project.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocRepository extends JpaRepository<Doc, Long> {
    List<Doc> findAllByProjectOrderByCreatedDateDesc(Project project);

    List<Doc> findAllByProjectAndNoticeOrderByCreatedDateDesc(Project project, boolean notice);

    List<Doc> findAllByProjectAndOnGoingOrderByCreatedDateDesc(Project project, boolean onGoing);
}
