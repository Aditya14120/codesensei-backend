package com.example.codesensei.service;

import com.example.codesensei.dto.auth.AuthResponse;
import com.example.codesensei.dto.auth.LoginRequest;
import com.example.codesensei.dto.auth.RegisterRequest;
import com.example.codesensei.entity.User;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);

    /** Revokes every token issued to this user before now, not just the caller's own. */
    void logout(User user);
}
