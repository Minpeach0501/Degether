package com.hanghae.degether.project.util;

import com.hanghae.degether.project.exception.ExceptionMessage;
import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.project.repository.ProjectRepository;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.model.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Configuration
@RequiredArgsConstructor
public class CommonUtil {

    public static User getUser(){
        UserDetailsImpl userDetails= (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userDetails == null) {
            throw new IllegalArgumentException(ExceptionMessage.REQUIRED_LOGIN);
        }
        return userDetails.getUser();
    }

    // Util
    public static Project getProject(Long projectId, ProjectRepository projectRepository) {
        return projectRepository.findById(projectId).orElseThrow(()-> new IllegalArgumentException(ExceptionMessage.NOT_EXIST_PROJECT));
    }
}
