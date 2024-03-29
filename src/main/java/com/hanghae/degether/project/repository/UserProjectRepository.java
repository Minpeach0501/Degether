package com.hanghae.degether.project.repository;

import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.project.model.UserProject;
import com.hanghae.degether.user.dto.MyProjectResDto;
import com.hanghae.degether.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProjectRepository extends JpaRepository<UserProject, Long> {

    boolean existsByProjectAndUserNot(Project project, User user);

    boolean existsByProjectAndUser(Project project, User user);
    boolean existsByProjectAndUserAndIsTeam(Project project, User user, boolean isTeam);

    int countByUserAndIsTeamAndProject_StepNot(User user, boolean isTeam,String step);


    Optional<UserProject> findByProjectAndUser(Project project, User user);


    List<UserProject> findTop3ByUserAndIsTeam(User user, boolean isTeam);



    List<UserProject> findAllByIsTeamAndUser(boolean isTeam, User user);
}
