package com.showmantra.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * The core security configuration class for the application.
 * 
 * This class sets up the Spring Security filter chain. It defines which API
 * endpoints are public (e.g., login, register, searching movies) and which
 * endpoints require authentication (e.g., booking seats, viewing history).
 * It also disables session creation since our API is completely stateless 
 * and relies entirely on JWT tokens for authenticating each request.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS using the corsConfigurationSource bean defined below
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Disable CSRF since we are using stateless JWTs (no session cookies)
            .csrf(csrf -> csrf.disable())
            // Configure route-level authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow public access to authentication endpoints (login, register)
                .requestMatchers("/api/users/login", "/api/users/register").permitAll()
                // Allow public access to browse movies, shows, theaters, and agent
                .requestMatchers("/api/movies/**", "/api/shows/**", "/api/theaters/**", "/api/agent/**").permitAll()
                // Allow public access to the default Spring error path
                .requestMatchers("/error").permitAll()
                // Require a valid JWT token for any other request (e.g. booking tickets)
                .anyRequest().authenticated()
            )
            // Configure session management to be stateless (do not store user info in server memory)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Insert our custom JWT filter before the standard Spring Security authentication filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow all origins (e.g. localhost:5173 for frontend)
        configuration.setAllowedOrigins(List.of("*"));
        // Allow common HTTP methods used in REST APIs
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Allow headers required for sending JSON and JWT tokens
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-User-Id"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this CORS configuration to all endpoints in the application
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
