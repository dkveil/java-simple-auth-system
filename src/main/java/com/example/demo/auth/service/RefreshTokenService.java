package com.example.demo.auth.service;

import com.example.demo.auth.entity.RefreshToken;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.jwt.JwtProperties;
import com.example.demo.auth.repo.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final JwtProperties          props;

    @Transactional
    public RefreshToken create(User user) {
        RefreshToken rt = RefreshToken.builder()
                .token(UUID.randomUUID().toString())          // prosty, losowy token
                .user(user)
                .expiryDate(LocalDateTime.now()
                             .plus(Duration.ofMillis(props.getRefreshExpMs())))
                .build();
        return repo.save(rt);
    }

    @Transactional
    public RefreshToken verify(String token) {
        RefreshToken rt = repo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (rt.isRevoked() || rt.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Refresh token expired or revoked");
        return rt;
    }

    @Transactional
    public RefreshToken rotate(RefreshToken old) {
        // unieważnij stary
        old.setRevoked(true);
        // utwórz nowy
        return create(old.getUser());
    }
}