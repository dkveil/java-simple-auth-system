package com.example.demo.auth.dto;

import java.util.Set;

public record UserInfoResponse(String username,
                               String email,
                               Set<String> roles,
                               String comment) {}