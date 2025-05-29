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
    private final RefreshTokenService  refreshService;

    public TokenPairResponse register(RegistrationRequest req) {
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

        var userDetails  = userDetailsService.loadUserByUsername(saved.getEmail());
        String accessJwt = jwtService.generateToken(userDetails);
        String refresh   = refreshService.create(saved).getToken();

        return new TokenPairResponse(accessJwt, refresh);
    }

    public TokenPairResponse login(LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        User user        = userRepo.findByEmail(req.email()).orElseThrow();
        String accessJwt = jwtService.generateToken(
                userDetailsService.loadUserByUsername(user.getEmail()));
        String refresh   = refreshService.create(user).getToken();

        return new TokenPairResponse(accessJwt, refresh);
    }

    public TokenPairResponse refresh(String refreshToken) {
        var valid   = refreshService.verify(refreshToken);
        var rotated = refreshService.rotate(valid);

        String newAccess = jwtService.generateToken(
                userDetailsService.loadUserByUsername(rotated.getUser().getEmail()));

        return new TokenPairResponse(newAccess, rotated.getToken());
    }
}