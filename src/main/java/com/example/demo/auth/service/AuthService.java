package com.example.demo.auth.service;

import com.example.demo.auth.dto.*;
import com.example.demo.auth.entity.*;
import com.example.demo.auth.jwt.JwtService;
import com.example.demo.auth.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository       userRepo;
    private final RoleRepository       roleRepo;
    private final PasswordEncoder      passwordEncoder;
    private final AuthenticationManager authManager;

    private final JwtService           jwtService;
    private final UserDetailsService   userDetailsService;

    public JwtResponse register(RegistrationRequest req) {
        if (userRepo.existsByEmail(req.email()))
            throw new IllegalArgumentException("E-mail already used");

        Role userRole = roleRepo.findByName(RoleName.USER)
                                .orElseThrow(() -> new IllegalStateException("ROLE_USER missing"));

        User saved = userRepo.save(
                User.builder()
                        .email(req.email())
                        .password(passwordEncoder.encode(req.password()))
                        .roles(Set.of(userRole))
                        .build()
        );

        String token = jwtService.generateToken(
                userDetailsService.loadUserByUsername(saved.getEmail()));
        return new JwtResponse(token);
    }

    public JwtResponse login(LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        String token = jwtService.generateToken(
                userDetailsService.loadUserByUsername(req.email()));
        return new JwtResponse(token);
    }
}