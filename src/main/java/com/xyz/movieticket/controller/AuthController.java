package com.xyz.movieticket.controller;

import com.xyz.movieticket.dto.request.LoginRequest;
import com.xyz.movieticket.dto.request.RegisterRequest;
import com.xyz.movieticket.dto.response.ApiResponse;
import com.xyz.movieticket.dto.response.AuthResponse;
import com.xyz.movieticket.model.User;
import com.xyz.movieticket.model.enums.UserRole;
import com.xyz.movieticket.repository.UserRepository;
import com.xyz.movieticket.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for user: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        AuthResponse response = AuthResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().toString())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email already registered"));
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.ROLE_CUSTOMER)
                .emailVerified(false)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String jwt = tokenProvider.generateToken(authentication);

        AuthResponse response = AuthResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().toString())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }
}