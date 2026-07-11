package com.example.codesensei.service;

import com.example.codesensei.dto.auth.AuthResponse;
import com.example.codesensei.dto.auth.LoginRequest;
import com.example.codesensei.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
