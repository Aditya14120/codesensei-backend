package com.example.codesensei.controller;

import com.example.codesensei.dto.auth.AuthResponse;
import com.example.codesensei.dto.auth.LoginRequest;
import com.example.codesensei.dto.auth.RegisterRequest;
import com.example.codesensei.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
