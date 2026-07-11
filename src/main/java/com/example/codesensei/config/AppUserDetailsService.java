package com.example.codesensei.config;

import com.example.codesensei.repository.UserRepository;
import com.example.codesensei.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Resolves the authenticated principal from the database by email (the JWT
 * subject). Used by JwtAuthFilter after a token's signature has already
 * been verified, so a lookup failure here means the user was deleted after
 * the token was issued.
 */
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("No user found for email: " + email));
    }
}
