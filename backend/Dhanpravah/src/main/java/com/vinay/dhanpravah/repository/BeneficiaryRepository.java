package com.vinay.dhanpravah.repository;

import com.vinay.dhanpravah.domain.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {
    Optional<Beneficiary> findByIdAndOwnerId(UUID uuid, Long ownerId);

    List<Beneficiary> findByOwnerId(Long ownerId);
}