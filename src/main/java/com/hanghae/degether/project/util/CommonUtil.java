package com.hanghae.degether.project.util;

import com.hanghae.degether.project.exception.ExceptionMessage;
import com.hanghae.degether.user.model.User;
import com.hanghae.degether.user.model.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class CommonUtil {
    public static User getUser(){
        UserDetailsImpl userDetails= (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userDetails == null) {
            throw new IllegalArgumentException(ExceptionMessage.REQUIRED_LOGIN);
        }
        return new User();
        // return userDetails.getUser();
    }
}
