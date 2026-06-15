package com.TMP.tms.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            
            // Explicitly enforce 401 and 403 responses per the document requirements
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> 
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthenticated request")) // 401
                .accessDeniedHandler((request, response, accessDeniedException) -> 
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient privileges")) // 403
            )
            
            // Stateless JWT sessions
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Endpoint Authorization Rules
            .authorizeHttpRequests(auth -> auth
                    // Allow Swagger UI for testing
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // === PROJECT AUTHORIZATION ===
                // Users get read-only access. Admins get all access.
                .requestMatchers(HttpMethod.GET, "/api/v1/projects/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers(HttpMethod.POST, "/api/v1/projects/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/projects/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/projects/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/projects/**").hasRole("ADMIN")

                // === TASK AUTHORIZATION ===
                // Users can create, read, and update tasks.
                .requestMatchers(HttpMethod.GET, "/api/v1/tasks/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers(HttpMethod.POST, "/api/v1/tasks/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/tasks/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/tasks/**").hasAnyRole("ADMIN", "USER")
                // Nobody but Admin can delete
                .requestMatchers(HttpMethod.DELETE, "/api/v1/tasks/**").hasRole("ADMIN")

                // Any other unmapped endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Add our custom JWT filter before standard authentication
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow your frontend's exact origin(s)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:63342",
                "http://127.0.0.1:63342",
                "http://127.0.0.1:5500"
        ));

        // Allow all standard HTTP methods including the OPTIONS preflight
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow standard headers that your frontend will send (like the Authorization header)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Apply these rules to every endpoint in this microservice
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}