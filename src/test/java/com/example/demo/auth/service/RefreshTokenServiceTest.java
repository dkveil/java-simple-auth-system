package com.example.demo.auth.service;

import com.example.demo.auth.entity.*;
import com.example.demo.auth.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback
class RefreshTokenServiceTest {

    @Autowired RefreshTokenService refreshTokenService;
    @Autowired UserRepository      userRepo;
    @Autowired RoleRepository      roleRepo;

    private User user;

    @BeforeEach
    void setUp() {
        Role role = roleRepo.findByName(RoleName.USER)
                            .orElseGet(() -> roleRepo.save(new Role(null, RoleName.USER)));

        userRepo.deleteAll();

        user = userRepo.save(
                User.builder()
                    .email("bob@test.com")
                    .password("x")
                    .roles(Set.of(role))
                    .build());
    }

    @Test
    void create_and_rotate_refresh_token() {
        var first  = refreshTokenService.create(user);
        var second = refreshTokenService.rotate(first);

        assertThat(first.isRevoked()).isTrue();
        assertThat(second.getToken()).isNotEqualTo(first.getToken());
    }
}