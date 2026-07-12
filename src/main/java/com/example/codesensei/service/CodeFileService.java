package com.example.codesensei.service;

import com.example.codesensei.entity.CodeFile;
import com.example.codesensei.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CodeFileService {
    CodeFile saveFile(MultipartFile file, User owner) throws Exception;
    List<CodeFile> getFilesForUser(User owner);
    CodeFile getFileForUser(String id, User owner);
    void deleteFileForUser(String id, User owner);
}
