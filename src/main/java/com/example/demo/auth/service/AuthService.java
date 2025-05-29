package com.example.demo.auth.service;

import com.example.demo.auth.dto.*;
import com.example.demo.auth.entity.*;
import com.example.demo.auth.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    public AuthResponse register(RegistrationRequest req) {
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

        return mapToResponse(saved);
    }

    public AuthResponse login(LoginRequest req) {

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        User user = userRepo.findByEmail(auth.getName())
                            .orElseThrow(() -> new IllegalStateException("User not found"));

        return mapToResponse(user);
    }

    private AuthResponse mapToResponse(User u) {
        Set<String> roles = u.getRoles()
                             .stream()
                             .map(r -> r.getName().name())
                             .collect(Collectors.toSet());
        return new AuthResponse(u.getEmail(), roles);
    }
}