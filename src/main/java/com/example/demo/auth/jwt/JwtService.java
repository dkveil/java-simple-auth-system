package com.example.demo.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties props;

    public String generateToken(UserDetails user) {

        Instant   now    = Instant.now();
        Instant   exp    = now.plusMillis(props.getExpirationMs());
        Date      issued = Date.from(now);
        Date      expire = Date.from(exp);

        return Jwts.builder()
                   .subject(user.getUsername())
                   .claim("roles",
                          user.getAuthorities()
                              .stream()
                              .map(GrantedAuthority::getAuthority)
                              .collect(Collectors.toList()))
                   .issuedAt(issued)
                   .expiration(expire)
                   .signWith(signKey(), Jwts.SIG.HS256)
                   .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        return user.getUsername().equals(extractUsername(token))
               && !isExpired(token);
    }

    private boolean isExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                   .verifyWith(signKey())
                   .build()
                   .parseSignedClaims(token)
                   .getPayload();
    }

    private SecretKey signKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(props.getSecret()));
    }
}