package com.hanghae.degether.User.security;



import com.hanghae.degether.User.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //계정이 잠겨있는지 않았는지 리턴. (true: 잠기지 않음)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    //계정이 만료되지 않았는지 리턴 (true: 만료 안됨)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    //계정이 활성화(사용가능)인지 리턴 (true: 활성화)
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override // 인가를 해주는 부분
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
}
