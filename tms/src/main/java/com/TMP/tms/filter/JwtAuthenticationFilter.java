package com.TMP.tms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            // 1. Validate the JWT using the shared secret (Stateless, no HTTP call)
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes())
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            // 2. Extract subject (userId) and role
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            if (userId != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Spring Security expects roles to start with "ROLE_"
                String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityName);

                // 3. Create the Authentication object with the Role
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(authority)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            logger.error("JWT Validation failed: " + e.getMessage());
            // Token is invalid/expired. We clear context to guarantee 401.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}