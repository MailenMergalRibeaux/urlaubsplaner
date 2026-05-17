package com.mmr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // change-password ist auch fuer Konten mit PASSWORT_AENDERUNG_ERFORDERLICH erreichbar
                        .requestMatchers("/api/auth/change-password").authenticated()
                        .requestMatchers(POST, "/api/mitarbeiter/**").hasRole("FUEHRUNGSKRAFT")
                        .requestMatchers(PUT, "/api/mitarbeiter/**").hasRole("FUEHRUNGSKRAFT")
                        .requestMatchers(DELETE, "/api/mitarbeiter/**").hasRole("FUEHRUNGSKRAFT")
                        // Alle anderen geschuetzten Endpoints: nur fuer normale Rollen, NICHT fuer
                        // Konten mit ausstehendem Passwortwechsel (die haben nur PASSWORT_AENDERUNG_ERFORDERLICH).
                        .anyRequest().hasAnyRole("MITARBEITER", "FUEHRUNGSKRAFT")
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
