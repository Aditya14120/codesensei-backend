package com.example.codesensei.service.impl;

import com.example.codesensei.entity.CodeFile;
import com.example.codesensei.entity.User;
import com.example.codesensei.repository.CodeFileRepository;
import com.example.codesensei.service.CodeFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * File bytes still live on local disk under ./uploads/ (fine for a single instance; swap for
 * S3/object storage if this ever runs on multiple instances or ephemeral disk). Metadata is now
 * persisted and owner-scoped, replacing the old in-memory, unowned index.
 */
@Service
public class CodeFileServiceImpl implements CodeFileService {

    private final Path uploadRoot;
    private final CodeFileRepository codeFileRepository;

    public CodeFileServiceImpl(CodeFileRepository codeFileRepository) throws IOException {
        this.codeFileRepository = codeFileRepository;
        this.uploadRoot = Paths.get("uploads");
        if (Files.notExists(uploadRoot)) {
            Files.createDirectories(uploadRoot);
        }
    }

    @Override
    public CodeFile saveFile(MultipartFile file, User owner) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String id = UUID.randomUUID().toString();
        String safeName = file.getOriginalFilename() == null
                ? "file"
                : file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
        String storedFileName = id + "_" + safeName;
        Path target = uploadRoot.resolve(storedFileName);

        try {
            Files.write(target, file.getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new IOException("Failed to save file", e);
        }

        CodeFile codeFile = CodeFile.builder()
                .filename(safeName)
                .storagePath(target.toAbsolutePath().toString())
                .size(file.getSize())
                .uploadedAt(Instant.now())
                .user(owner)
                .build();

        return codeFileRepository.save(codeFile);
    }

    @Override
    public List<CodeFile> getFilesForUser(User owner) {
        return codeFileRepository.findByUser(owner);
    }

    @Override
    public CodeFile getFileForUser(String id, User owner) {
        return codeFileRepository.findByIdAndUser(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
    }

    @Override
    public void deleteFileForUser(String id, User owner) {
        CodeFile file = getFileForUser(id, owner);

        codeFileRepository.delete(file);

        try {
            Files.deleteIfExists(Paths.get(file.getStoragePath()));
        } catch (IOException e) {
            // The DB row (the source of truth for ownership/listing) is already gone; a leftover
            // orphaned file on disk isn't worth failing the request over.
        }
    }
}
