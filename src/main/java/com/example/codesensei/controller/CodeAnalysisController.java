package com.example.codesensei.controller;

import com.example.codesensei.model.CodeAnalysisRequest;
import com.example.codesensei.model.CodeAnalysisResponse;
import com.example.codesensei.service.CodeAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
@CrossOrigin(origins = "*") // allow frontend requests
public class CodeAnalysisController {

    private final CodeAnalysisService codeAnalysisService;

    public CodeAnalysisController(CodeAnalysisService codeAnalysisService) {
        this.codeAnalysisService = codeAnalysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<CodeAnalysisResponse> analyzeCode(@Valid @RequestBody CodeAnalysisRequest request) {
        CodeAnalysisResponse response = codeAnalysisService.analyzeCode(request.getCode());
        return ResponseEntity.ok(response);
    }
}
