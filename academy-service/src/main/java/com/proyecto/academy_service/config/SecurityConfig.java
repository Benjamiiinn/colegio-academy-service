package com.proyecto.academy_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtValidationFilter jwtValidationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Solo Admin o Docente pueden poner notas
                .requestMatchers(HttpMethod.POST, "/api/v1/calificaciones/**").hasAnyRole("ADMIN", "DOCENTE")
                
                // Solo Admin puede crear cursos o matricular
                .requestMatchers(HttpMethod.POST, "/api/v1/cursos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/matriculas/**").hasRole("ADMIN")
                
                // Cualquiera autenticado puede leer datos (GET)
                .requestMatchers(HttpMethod.GET, "/api/v1/**").authenticated()
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtValidationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
