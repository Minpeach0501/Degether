package com.hanghae.degether.doc;

import com.hanghae.degether.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocRepository extends JpaRepository<Doc, Long> {
    List<Doc> findAllByProjectOrderByCreatedDateDesc(Project project);
}
