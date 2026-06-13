package com.TMP.uas.configuration;

import com.TMP.uas.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // Default to 24h (86400000 ms) if not set
    private long jwtExpirationInMs;

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // Convert the string secret into a cryptographic key for HS256
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

        return Jwts.builder()
                // 1. Subject (userId)
                .setSubject(user.getId().toString())
                // 2. Role (Added as a custom claim)
                .claim("role", user.getRole().name())
                // 3. Issued-at
                .setIssuedAt(now)
                // 4. Expiry
                .setExpiration(expiryDate)
                // 5. Signature (HS256)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}