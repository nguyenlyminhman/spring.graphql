package com.spring.graphql.service;


import com.spring.graphql.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
@Service
@Slf4j
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // Tạo Access Token ===========================================
    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("email",  user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .id(UUID.randomUUID().toString())   // JWT ID - unique per token
                .signWith(getSigningKey())
                .compact();
    }

    // Validate và lấy username ===========================================
    /**
     * Parse và verify token.
     *
     * @return username (subject) nếu token hợp lệ
     * @throws ExpiredJwtException nếu token hết hạn
     * @throws JwtException        nếu token không hợp lệ
     */
    public String validateAndExtractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}