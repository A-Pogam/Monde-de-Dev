package com.openclassrooms.p6.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Optional;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKeyString;

    private Key secretKey;

    @PostConstruct
    public void init() {
        // Convertit la clé String en clé HMAC utilisable
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    public String generateJwtToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Optional<Long> extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(secretKey)
                    .build().parseClaimsJws(token).getBody();
            return Optional.of(Long.parseLong(claims.getSubject()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static String extractJwtFromHeader(String header) {
        return header.substring(7); // retire "Bearer "
    }
}
