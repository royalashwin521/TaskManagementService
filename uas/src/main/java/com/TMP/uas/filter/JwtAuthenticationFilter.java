package  com.TMP.uas.filter;

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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/register") ||
                path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 1. Check if token exists
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            // 2. Validate token and extract claims
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes())
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            // 3. Set the authenticated user in the Security Context
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                String authorityName = role != null && role.startsWith("ROLE_") ? role : "ROLE_" + role;
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityName);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(authority)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            logger.error("JWT Validation failed: " + e.getMessage());
            SecurityContextHolder.clearContext(); // Ensure context is wiped on bad token
        }

        // 4. Continue the request
        filterChain.doFilter(request, response);
    }
}