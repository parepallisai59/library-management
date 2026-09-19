package com.library.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // Public pages and authentication
                .requestMatchers(
                    "/",
                    "/login",
                    "/register",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/users/register",
                    "/users/login"
                ).permitAll()

                // Admin pages
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")

                // Only ADMIN can add books
                .requestMatchers(HttpMethod.POST, "/books/**")
                .hasRole("ADMIN")

                // Only ADMIN can edit books
                .requestMatchers(HttpMethod.PUT, "/books/**")
                .hasRole("ADMIN")

                // Only ADMIN can delete books
                .requestMatchers(HttpMethod.DELETE, "/books/**")
                .hasRole("ADMIN")

                // User pages
                .requestMatchers("/user/**")
                .hasRole("USER")

                // Everything else requires authentication
                .anyRequest()
                .authenticated()
            )

            .formLogin(form -> form.disable())

            .logout(logout -> logout
                .logoutUrl("/users/logout")
            );

        return http.build();
    }
}