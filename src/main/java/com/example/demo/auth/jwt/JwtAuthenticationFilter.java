package com.example.demo.auth.jwt;

import com.example.demo.auth.service.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService          jwtService;
    private final RefreshTokenService refreshService;
    private final UserDetailsService  userDetailsService;
    private final JwtProperties       props;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest  request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain         chain)
            throws ServletException, IOException {

        String token = extractAccessToken(request);

        boolean authenticated = false;

        if (token != null) {
            String username = jwtService.extractUsername(token);
            if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails user = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, user)) {
                    setAuth(user, request);
                    authenticated = true;
                }
            }
        }

        if (!authenticated) {
            String refreshToken = extractCookie(request.getCookies(), "refreshToken");

            if (refreshToken != null) {
                try {
                    var validRT = refreshService.verify(refreshToken);
                    var rotated = refreshService.rotate(validRT);

                    UserDetails user = userDetailsService
                            .loadUserByUsername(rotated.getUser().getEmail());

                    String newAccess = jwtService.generateToken(user);

                    setAuth(user, request);
                    attachTokens(response, newAccess, rotated.getToken());
                } catch (Exception ignored) {
                }
            }
        }
        chain.doFilter(request, response);
    }

    /* ---------- helpers ---------- */

    private String extractAccessToken(HttpServletRequest req) {
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer "))
            return header.substring(7);

        return extractCookie(req.getCookies(), "accessToken");
    }

    private String extractCookie(Cookie[] cookies, String name) {
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                     .filter(c -> name.equals(c.getName()))
                     .findFirst()
                     .map(Cookie::getValue)
                     .orElse(null);
    }

    private void setAuth(UserDetails user, HttpServletRequest req) {
        var auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void attachTokens(HttpServletResponse resp,
                              String newAccess,
                              String newRefresh) {

        ResponseCookie accessC = ResponseCookie.from("accessToken", newAccess)
                .httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(props.getExpirationMs()/1000)
                .build();

        ResponseCookie refreshC = ResponseCookie.from("refreshToken", newRefresh)
                .httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(props.getRefreshExpMs()/1000)
                .build();

        resp.addHeader(HttpHeaders.SET_COOKIE, accessC.toString());
        resp.addHeader(HttpHeaders.SET_COOKIE, refreshC.toString());
        resp.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccess);
    }
}