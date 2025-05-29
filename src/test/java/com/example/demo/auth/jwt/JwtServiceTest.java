package com.example.demo.auth.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtServiceTest {

    @Autowired JwtService jwt;

    @Test
    void generated_token_is_valid_and_contains_username() {
        var user = new User(
                "alice@example.com",
                "pass",
                java.util.Set.of(new SimpleGrantedAuthority("ROLE_USER")));

        String token = jwt.generateToken(user);

        assertThat(jwt.extractUsername(token)).isEqualTo("alice@example.com");
        assertThat(jwt.isTokenValid(token, user)).isTrue();
    }
}