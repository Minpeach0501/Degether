package com.hanghae.degether.doc.repository;


import com.hanghae.degether.doc.model.Folder;
import com.hanghae.degether.project.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findAllByProject(Project project);
}
