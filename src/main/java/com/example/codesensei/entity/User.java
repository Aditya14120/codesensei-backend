package com.example.codesensei.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;

    private String role; // ROLE_USER, ROLE_ADMIN

    /**
     * Tokens issued before this instant are rejected even if not yet expired — set on logout
     * so a stolen/leaked token can actually be revoked instead of remaining valid until its
     * natural expiry. Null means no cutoff (no logout has happened yet).
     */
    private Instant tokensValidAfter;
}
