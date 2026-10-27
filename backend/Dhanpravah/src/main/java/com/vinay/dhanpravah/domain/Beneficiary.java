package com.vinay.dhanpravah.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "beneficiary",
        uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "account_number"}))
@Data
@Builder
public class Beneficiary {

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Column(nullable = false)
    private String name;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    private String nickname;

    @Column(nullable = false)
    private boolean verified = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

}
