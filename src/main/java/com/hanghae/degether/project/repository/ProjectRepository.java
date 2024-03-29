package com.hanghae.degether.project.repository;

import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

}
