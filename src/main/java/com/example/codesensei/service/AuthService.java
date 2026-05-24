package com.example.codesensei.service;

import com.example.codesensei.dto.auth.LoginRequest;
import com.example.codesensei.dto.auth.RegisterRequest;
import com.example.codesensei.entity.User;

public interface AuthService {
    User register(RegisterRequest request);
    User login(LoginRequest request);
}
