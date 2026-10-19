package com.ateeb.enterprise_platform.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final Long companyId;
    private final String email;
    private final String password;
    private final boolean mustChangePassword;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(
            Long userId,
            Long companyId,
            String email,
            String password,
            boolean mustChangePassword,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities
    ){
        this.userId = userId;
        this.companyId = companyId;
        this.email = email;
        this.password = password;
        this.mustChangePassword = mustChangePassword;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }


}
