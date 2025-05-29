package com.example.demo.auth.controller;

import com.example.demo.auth.dto.*;
import com.example.demo.auth.jwt.JwtProperties;
import com.example.demo.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService   authService;
    private final JwtProperties props;

    private ResponseEntity<ApiMessageResponse> withCookies(TokenPairResponse pair,
                                                           String msg) {

        ResponseCookie accessC = ResponseCookie.from("accessToken", pair.accessToken())
                .httpOnly(true).secure(true).sameSite("Strict").path("/")
                .maxAge(props.getExpirationMs() / 1000).build();

        ResponseCookie refreshC = ResponseCookie.from("refreshToken", pair.refreshToken())
                .httpOnly(true).secure(true).sameSite("Strict").path("/")
                .maxAge(props.getRefreshExpMs() / 1000).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessC.toString())
                .header(HttpHeaders.SET_COOKIE, refreshC.toString())
                .body(new ApiMessageResponse(msg));
    }

    @Operation(
        summary = "Register new user",
        description = "Creates user, sets HttpOnly access & refresh cookies",
        responses = @ApiResponse(responseCode = "200", description = "Registered")
    )
    @PostMapping("/register")
    public ResponseEntity<ApiMessageResponse> register(@RequestBody RegistrationRequest req) {
        return withCookies(authService.register(req), "Zarejestrowano pomyślnie");
    }

    @Operation(
        summary = "Login",
        description = "Authenticates user, sets HttpOnly cookies with JWT pair"
    )
    @PostMapping("/login")
    public ResponseEntity<ApiMessageResponse> login(@RequestBody LoginRequest req) {
        return withCookies(authService.login(req), "Zalogowano pomyślnie");
    }

    @Operation(
        summary = "Refresh token pair",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiMessageResponse> refresh(
            @CookieValue("refreshToken") String rt) {

        return withCookies(authService.refresh(rt), "Odświeżono tokeny");
    }

    @Operation(
        summary = "Logout",
        description = "Clears JWT cookies"
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiMessageResponse> logout() {
        ResponseCookie del1 = ResponseCookie.from("accessToken", "")
                .httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(0).build();
        ResponseCookie del2 = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(0).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, del1.toString())
                .header(HttpHeaders.SET_COOKIE, del2.toString())
                .body(new ApiMessageResponse("Wylogowano"));
    }

    @Operation(
        summary = "Current user info",
        description = "Returns info for authenticated user (or message when anonymous)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<?> currentUser(@AuthenticationPrincipal UserDetails principal) {

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