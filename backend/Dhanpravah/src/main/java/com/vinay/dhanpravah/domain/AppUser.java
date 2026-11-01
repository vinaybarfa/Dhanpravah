package com.vinay.dhanpravah.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "app_user")
@Data
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "mobile_number", unique = true)
    private String mobileNumber;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public void updatePassword(Object hash) {
    }

    public void updateProfile(String trim, String trim1) {
    }
}
