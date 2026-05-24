package com.example.codesensei.service.impl;

import com.example.codesensei.model.CodeFile;
import com.example.codesensei.service.CodeFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal working implementation:
 * - Saves uploaded files to ./uploads/<uuid>_<originalName>
 * - Keeps an in-memory index (ConcurrentHashMap) for metadata.
 *
 * NOTE:
 * - This is suitable for development/testing.
 * - For production, replace with DB persistence and S3 (or similar).
 */
@Service
public class CodeFileServiceImpl implements CodeFileService {

    private final Path uploadRoot;
    private final Map<String, CodeFile> store = new ConcurrentHashMap<>();

    public CodeFileServiceImpl() throws IOException {
        this.uploadRoot = Paths.get("uploads");
        if (Files.notExists(uploadRoot)) {
            Files.createDirectories(uploadRoot);
        }
    }

    @Override
    public CodeFile saveFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String id = UUID.randomUUID().toString();
        String safeName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
        String storedFileName = id + "_" + safeName;
        Path target = uploadRoot.resolve(storedFileName);

        // Save bytes to disk
        try {
            Files.write(target, file.getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new IOException("Failed to save file", e);
        }

        CodeFile codeFile = new CodeFile(id, safeName, target.toAbsolutePath().toString(), file.getSize(), Instant.now());
        store.put(id, codeFile);
        return codeFile;
    }

    @Override
    public List<CodeFile> getAllFiles() {
        return new ArrayList<>(store.values());
    }

    @Override
    public CodeFile getFileById(String id) {
        CodeFile f = store.get(id);
        if (f == null) throw new RuntimeException("File not found: " + id);
        return f;
    }

    @Override
    public void deleteFile(String id) {
        CodeFile f = store.remove(id);
        if (f == null) throw new RuntimeException("File not found: " + id);

        try {
            Path p = Paths.get(f.getStoragePath());
            Files.deleteIfExists(p);
        } catch (IOException e) {
            // If delete fails, log or rethrow — for now, ignore to keep API simple
            // throw new RuntimeException("Failed to delete file: " + id, e);
        }
    }
}
