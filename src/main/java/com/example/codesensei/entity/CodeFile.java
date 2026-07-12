package com.example.codesensei.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "code_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String filename;

    /** Local path (or future S3 key) — never exposed to clients, see CodeFileResponse. */
    @Column(columnDefinition = "TEXT")
    private String storagePath;

    private long size;

    private Instant uploadedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
