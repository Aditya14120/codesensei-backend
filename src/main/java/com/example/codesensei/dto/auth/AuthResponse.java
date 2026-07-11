package com.example.codesensei.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for register/login. Deliberately excludes the password hash
 * that the User entity carries.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String id;
    private String name;
    private String email;
    private String role;
    private String token;
}
