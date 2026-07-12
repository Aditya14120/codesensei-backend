package com.example.codesensei.repository;

import com.example.codesensei.entity.CodeFile;
import com.example.codesensei.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodeFileRepository extends JpaRepository<CodeFile, String> {

    List<CodeFile> findByUser(User user);

    // Scoping by owner in the query itself means a caller can never even attempt to load
    // or delete another user's file by guessing its id.
    Optional<CodeFile> findByIdAndUser(String id, User user);
}
