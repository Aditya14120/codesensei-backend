package com.example.codesensei.controller;

import com.example.codesensei.model.CodeFile;
import com.example.codesensei.service.CodeFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections; // Import Collections if you want to return an empty list on error
import java.util.List;

@RestController
@RequestMapping("/api/code-files")
public class CodeFileController {

    @Autowired
    private CodeFileService codeFileService;

    // Upload a code file
    @PostMapping("/upload")
    public ResponseEntity<CodeFile> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            CodeFile savedFile = codeFileService.saveFile(file);
            return ResponseEntity.ok(savedFile);
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("Error uploading file: " + e.getMessage());
            return ResponseEntity.internalServerError().build(); // Return 500 Internal Server Error
        }
    }

    // Get all uploaded files
    @GetMapping
    public ResponseEntity<List<CodeFile>> getAllFiles() {
        try {
            // Line 22: This is where the error was, now wrapped in try-catch
            List<CodeFile> files = codeFileService.getAllFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("Error retrieving all files: " + e.getMessage());
            // Return an appropriate error response, e.g., 500 Internal Server Error
            // You might want to return an empty list or a specific error object depending on your API design
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Get a file by ID
    @GetMapping("/{id}")
    public ResponseEntity<CodeFile> getFileById(@PathVariable String id) {
        try {
            CodeFile file = codeFileService.getFileById(id);
            if (file != null) {
                return ResponseEntity.ok(file);
            } else {
                return ResponseEntity.notFound().build(); // Return 404 if file not found
            }
        } catch (Exception e) {
            System.err.println("Error retrieving file by ID: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Delete a file by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        try {
            codeFileService.deleteFile(id);
            return ResponseEntity.noContent().build(); // 204 No Content for successful deletion
        } catch (Exception e) {
            System.err.println("Error deleting file: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}