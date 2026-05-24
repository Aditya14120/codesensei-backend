package com.example.codesensei.service;

import com.example.codesensei.model.CodeFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CodeFileService {
    CodeFile saveFile(MultipartFile file) throws Exception;
    List<CodeFile> getAllFiles();
    CodeFile getFileById(String id) throws RuntimeException;
    void deleteFile(String id) throws RuntimeException;
}
