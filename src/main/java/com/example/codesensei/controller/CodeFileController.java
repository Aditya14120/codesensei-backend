package com.example.codesensei.controller;

import com.example.codesensei.dto.codefile.CodeFileResponse;
import com.example.codesensei.entity.CodeFile;
import com.example.codesensei.security.CustomUserDetails;
import com.example.codesensei.service.CodeFileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/code-files")
public class CodeFileController {

    private final CodeFileService codeFileService;

    public CodeFileController(CodeFileService codeFileService) {
        this.codeFileService = codeFileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<CodeFileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails principal) throws Exception {
        CodeFile saved = codeFileService.saveFile(file, principal.getUser());
        return ResponseEntity.ok(toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<CodeFileResponse>> getAllFiles(@AuthenticationPrincipal CustomUserDetails principal) {
        List<CodeFileResponse> files = codeFileService.getFilesForUser(principal.getUser())
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodeFileResponse> getFileById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        CodeFile file = codeFileService.getFileForUser(id, principal.getUser());
        return ResponseEntity.ok(toResponse(file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        codeFileService.deleteFileForUser(id, principal.getUser());
        return ResponseEntity.noContent().build();
    }

    private CodeFileResponse toResponse(CodeFile file) {
        return new CodeFileResponse(file.getId(), file.getFilename(), file.getSize(), file.getUploadedAt());
    }
}
