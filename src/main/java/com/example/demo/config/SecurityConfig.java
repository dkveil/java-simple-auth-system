package com.example.demo.config;

import com.example.demo.auth.entity.Role;
import com.example.demo.auth.entity.RoleName;
import com.example.demo.auth.jwt.JwtAuthenticationFilter;
import com.example.demo.auth.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepo;

    @Bean
    UserDetailsService userDetailsService() {
        return email -> userRepo.findByEmail(email)
                .map(u -> new User(
                        u.getEmail(),
                        u.getPassword(),
                        u.getRoles().stream()
                                .map(this::asAuthority)
                                .collect(Collectors.toSet())
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private SimpleGrantedAuthority asAuthority(Role role) {
        return new SimpleGrantedAuthority("ROLE_" + role.getName().name());
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .csrf(cs -> cs.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/auth/**",
                            "/v3/api-docs/**",
                            "/swagger-ui.html",
                            "/swagger-ui/**"
                    ).permitAll()
                    .requestMatchers(HttpMethod.GET, "/admin/**").hasRole(RoleName.ADMIN.name())
                    .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}