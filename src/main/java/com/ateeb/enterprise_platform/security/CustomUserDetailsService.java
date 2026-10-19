package com.ateeb.enterprise_platform.security;

import com.ateeb.enterprise_platform.domain.entity.User;
import com.ateeb.enterprise_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new
                        UsernameNotFoundException("Username not found with email: " + email));

        Set<GrantedAuthority> authorities = new HashSet<>();

        user.getRoles().forEach(role -> {
            authorities.add(new
                    SimpleGrantedAuthority("ROLE_"+ role.getName()));

            role.getPermissions().forEach(permission ->
                    authorities.add(new
                            SimpleGrantedAuthority(permission.name()))
                    );
        });

        boolean isAccountActive = user.isEnabled() &&
                user.getCompany().isActive();

        return new CustomUserDetails(
                user.getId(),
                user.getCompany().getId(),
                user.getEmail(),
                user.getPassword(),
                user.isMustChangePassword(),
                isAccountActive,
                authorities
        );
    }
}
