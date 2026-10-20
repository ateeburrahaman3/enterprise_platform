package com.ateeb.enterprise_platform.service;

import com.ateeb.enterprise_platform.domain.entity.User;
import com.ateeb.enterprise_platform.repository.UserRepository;
import com.ateeb.enterprise_platform.security.CustomUserDetails;
import com.ateeb.enterprise_platform.security.JwtService;
import com.ateeb.enterprise_platform.web.dto.request.ChangePasswordRequest;
import com.ateeb.enterprise_platform.web.dto.request.LoginRequest;
import com.ateeb.enterprise_platform.web.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.security.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );


        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();


        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                "Bearer",
                jwtExpirationMs / 1000, // Return seconds
                userDetails.getUsername(),
                userDetails.isMustChangePassword()
        );
    }

    @Transactional
    public AuthResponse changePassword(CustomUserDetails currentUser, ChangePasswordRequest request) {
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(()-> new
                        IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password does not match");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as the old password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        user.setMustChangePassword(false);
        userRepository.save(user);

        CustomUserDetails updateDetails = new CustomUserDetails(
                user.getId(),
                user.getCompany().getId(),
                user.getEmail(),
                user.getPassword(),
                false,
                user.isEnabled(),
                currentUser.getAuthorities()
        );

        String newToken = jwtService.generateToken(updateDetails);

        return new AuthResponse(
                newToken,
                "Bearer",
                jwtExpirationMs/1000,
                user.getEmail(),
                false
        );
    }
}