package com.example.codesensei.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class AppUserDetailsService {

    @Bean
    public UserDetailsService userDetailsService() {
        // temporary — tells Spring we manage users ourselves
        return username -> {
            throw new RuntimeException("No default users");
        };
    }
}
