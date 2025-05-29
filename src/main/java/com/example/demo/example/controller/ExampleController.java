package com.example.demo.example.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.auth.dto.UserInfoResponse;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/example")
@RequiredArgsConstructor
public class ExampleController {

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails principal) {

        if (principal == null)
            return ResponseEntity.ok("Nie jesteś zalogowany");

        Set<String> roles = principal.getAuthorities().stream()
                                     .map(GrantedAuthority::getAuthority)
                                     .collect(Collectors.toSet());

        String msg = roles.contains("ROLE_ADMIN")
                    ? "Zalogowany jako administrator"
                    : "Zalogowany";

        return ResponseEntity.ok(
                new UserInfoResponse(
                        principal.getUsername(),
                        principal.getUsername(),
                        roles,
                        msg));
    }
}