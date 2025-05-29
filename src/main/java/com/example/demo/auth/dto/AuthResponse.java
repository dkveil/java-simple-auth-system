package com.example.demo.auth.dto;

import java.util.Set;

public record AuthResponse(String email, Set<String> roles) {}