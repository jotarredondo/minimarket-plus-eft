package com.duoc.minimarket.auth_service.config;

import com.duoc.minimarket.auth_service.security.CustomUserDetailsService;
import com.duoc.minimarket.auth_service.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customUserDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationProvider authenticationProvider
    ) throws Exception {

        http
                // API REST stateless: no se utilizará CSRF ni sesiones.
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        // Registro y login son públicos.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // Documentación OpenAPI y Swagger UI.
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Consola H2 habilitada durante desarrollo.
                        .requestMatchers("/h2-console/**").permitAll()

                        // Todos los demás endpoints requieren JWT.
                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider)

                // Nuestro filtro JWT se ejecuta antes del filtro estándar.
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // Necesario para visualizar la consola H2.
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }
}