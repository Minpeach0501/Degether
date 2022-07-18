package com.hanghae.degether.project.util;

import com.hanghae.degether.project.exception.CustomException;
import com.hanghae.degether.project.exception.ErrorCode;
import com.hanghae.degether.project.repository.ProjectRepository;
import com.hanghae.degether.project.model.Project;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.security.JwtTokenProvider;
import com.hanghae.degether.user.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@RequiredArgsConstructor
public class CommonUtil {

    public static User getUser(){
        try {
            UserDetailsImpl userDetails= (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userDetails == null) {
                throw new CustomException(ErrorCode.REQUIRED_LOGIN);
            }
            return userDetails.getUser();
        } catch (ClassCastException e) {
            throw new CustomException(ErrorCode.REQUIRED_LOGIN);
        }
    }
    public static User getUserByToken(String token, JwtTokenProvider jwtTokenProvider){
        if(token == null || "null".equals(token)) return null;
        UserDetailsImpl userDetails = (UserDetailsImpl) jwtTokenProvider.getAuthentication(token).getPrincipal();
        return userDetails.getUser();
    }

    // Util
    public static Project getProject(Long projectId, ProjectRepository projectRepository) {
        return projectRepository.findById(projectId).orElseThrow(()->
                // new IllegalArgumentException(ExceptionMessage.NOT_EXIST_PROJECT)
                new CustomException(ErrorCode.NOT_EXIST_PROJECT)
        );
    }
}
