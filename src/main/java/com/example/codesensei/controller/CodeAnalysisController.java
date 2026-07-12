package com.example.codesensei.controller;

import com.example.codesensei.entity.User;
import com.example.codesensei.model.CodeAnalysisRequest;
import com.example.codesensei.model.CodeAnalysisResponse;
import com.example.codesensei.model.TranslateRequest;
import com.example.codesensei.model.TranslateResponse;
import com.example.codesensei.security.CustomUserDetails;
import com.example.codesensei.service.CodeAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
public class CodeAnalysisController {

    private final CodeAnalysisService codeAnalysisService;

    public CodeAnalysisController(CodeAnalysisService codeAnalysisService) {
        this.codeAnalysisService = codeAnalysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<CodeAnalysisResponse> analyzeCode(@Valid @RequestBody CodeAnalysisRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        User currentUser = principal.getUser();
        CodeAnalysisResponse response = codeAnalysisService.analyzeCode(
                request.getCode(), request.getLanguage(), currentUser, request.getMode());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/translate")
    public ResponseEntity<TranslateResponse> translateCode(@Valid @RequestBody TranslateRequest request) {
        String translatedCode = codeAnalysisService.translateCode(
                request.getCode(), request.getSourceLanguage(), request.getTargetLanguage());
        return ResponseEntity.ok(
                new TranslateResponse(translatedCode, request.getSourceLanguage(), request.getTargetLanguage()));
    }
}
